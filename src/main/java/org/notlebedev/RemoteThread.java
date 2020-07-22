package org.notlebedev;

import org.notlebedev.exceptions.OperationFailedException;
import org.notlebedev.introspection.exceptions.InaccessiblePackageException;
import org.notlebedev.introspection.ObjectIntrospection;
import org.notlebedev.introspection.exceptions.SyntheticClassException;
import org.notlebedev.networking.MasterConnection;
import org.notlebedev.networking.messages.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Class providing functionality of remote code execution with similar
 * interface to execution {@link Runnable} objects via {@link Thread} classes.
 * Object type of {@link Remote} (payload) will be uploaded to remote server and
 * executed there async. After execution payload will be retrieved and original
 * payload will be replaced. Note that this replacement will be shallow copying
 * objects fields, so references to the object will remain correct, however
 * references to values stored in it will become obsolete
 */
public class RemoteThread {
    private final MasterConnection connection;
    private final Remote payload;
    private final Operation operation;

    public RemoteThread(MasterConnection masterConnection, Remote payload) throws SyntheticClassException {
        this.connection = masterConnection;
        this.payload = payload;
        if (payload.getClass().isSynthetic())
            throw new SyntheticClassException();
        operation = new Operation();
    }

    /**
     * Causes this thread to start execution on local machine, send object and
     * all related classes to slave server, execute {@link Remote#run} method
     * there and return the object back
     */
    public void start() {
        operation.start();
    }

    /**
     * Wait for remote execution to finish and payload to be updated
     *
     * @throws InterruptedException if there is any interruption in current
     *                              Thread
     */
    public void join() throws InterruptedException {
        operation.join();
    }

    /**
     * Check if execution of the payload went successfully, if it did not
     * {@link Exception} that resulted in failure can be found via
     * {@link #getException} method
     *
     * @return true if no exceptions occurred during operation, false if
     * connection was closed, slave failed to perform some operation
     * or classes necessary for deserialization were not found
     */
    public boolean isSuccessful() {
        return operation.isSuccessful();
    }

    /**
     * Retrieve exception stopped execution
     *
     * @return {@link OperationFailedException}, {@link IOException},
     * {@link ClassNotFoundException} if exception occurred or null if not
     */
    public Exception getException() {
        return operation.getException();
    }

    /**
     * Get payload provided to the constructor of class
     *
     * @return the same object as provided in constructor (see class description
     * for more details on effect of remote execution on payload)
     */
    public Runnable getPayload() {
        return payload;
    }

    /**
     * Set behavior in case module can not be accessed for introspection.
     * This can be set to info level, or totally suppressed in case
     * inaccessibility of modules is expected and is used as a stop for
     * introspection, or to error and error + exception if such behavior is
     * not desired
     *
     * @param inaccessibleModulePolicy check {@link ObjectIntrospection.InaccessibleModulePolicy}
     *                                 for description of options
     */
    public void setInaccessibleModulePolicy(ObjectIntrospection.InaccessibleModulePolicy inaccessibleModulePolicy) {
        operation.setInaccessibleModulePolicy(inaccessibleModulePolicy);
    }

    /**
     * Should annotations classes be sent to remote VM, or should they be ignored
     * true by default
     * @param inspectAnnotations true -- send, false -- ignore
     */
    public void setInspectAnnotations(boolean inspectAnnotations) {
        operation.setInspectAnnotations(inspectAnnotations);
    }

    private class Operation extends Thread {
        private Exception e;
        private final AtomicReference<ObjectIntrospection> objectIntrospection = new AtomicReference<>();
        private final AtomicReference<ObjectIntrospection.InaccessibleModulePolicy> policy = new AtomicReference<>();
        private final AtomicBoolean inspectAnnotations = new AtomicBoolean(true);

        public Operation() {
            policy.set(ObjectIntrospection.InaccessibleModulePolicy.WARN);
        }

        synchronized public void
        setInaccessibleModulePolicy(ObjectIntrospection.InaccessibleModulePolicy inaccessibleModulePolicy) {
            if(objectIntrospection.get() != null)
                objectIntrospection.get().setInaccessibleModulePolicy(inaccessibleModulePolicy);
            policy.set(inaccessibleModulePolicy);
        }

        synchronized public void setInspectAnnotations(boolean inspectAnnotations) {
            if(objectIntrospection.get() != null)
                objectIntrospection.get().setInspectAnnotations(inspectAnnotations);
            this.inspectAnnotations.set(inspectAnnotations);
        }

        @Override
        public void run() {
            try {
                exec();
            } catch (OperationFailedException | IOException | ClassNotFoundException | InaccessiblePackageException e) {
                this.e = e;
            }
        }

        private void exec() throws IOException, ClassNotFoundException, OperationFailedException, InaccessiblePackageException {
            AbstractMessage response = connection.sendRequest(new GetExecutionContextMessage());
            if (!(response instanceof SendExecutionContextMessage))
                throw new OperationFailedException();
            SendExecutionContextMessage resp = (SendExecutionContextMessage) response;
            Set<Class<?>> omitClasses = resp.getClassNames().stream().map(s -> {
                try {
                    return Class.forName(s);
                } catch (ClassNotFoundException e) {
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toCollection(HashSet::new));

            try {
                objectIntrospection.set(new ObjectIntrospection(payload, omitClasses));
            } catch (SyntheticClassException e) {
                throw new IllegalStateException("Class was expected to be non-synthetic");
            }
            objectIntrospection.get().setInaccessibleModulePolicy(policy.get());
            objectIntrospection.get().setInspectAnnotations(inspectAnnotations.get());

            response = connection.sendRequest(new LoadClassesMessage(ExecutionContext
                    .toBytecodes(objectIntrospection.get().getClassesUsed())));
            if (!(response instanceof OperationSuccessfulMessage))
                throw new OperationFailedException();

            var objDump = new ObjectDump(payload);
            Map<String, byte[]> obj = new HashMap<>();
            obj.put(objDump.getName(), objDump.getObjectData());
            response = connection.sendRequest(new SendObjectsMessage(obj));
            if (!(response instanceof OperationSuccessfulMessage))
                throw new OperationFailedException();

            response = connection.sendRequest(new ExecuteRunnableMessage());
            if (!(response instanceof OperationSuccessfulMessage))
                throw new OperationFailedException();

            response = connection.sendRequest(new GetObjectsMessage(1));
            if (!(response instanceof SendObjectsMessage) || ((SendObjectsMessage) response).getObjects().size() != 1)
                throw new OperationFailedException();

            ByteArrayInputStream bis = new ByteArrayInputStream(((SendObjectsMessage) response)
                    .getObjects().values().toArray(byte[][]::new)[0]);
            CustomClassLoaderObjectInputStream objectInputStream;
            ByteArrayClassLoader threadClassLoader
                    = new ByteArrayClassLoader(ClassLoader.getSystemClassLoader());
            objectInputStream = new CustomClassLoaderObjectInputStream(bis, threadClassLoader);
            Object executionResult = objectInputStream.readObject();
            if (!executionResult.getClass().equals(payload.getClass()))
                throw new OperationFailedException();

            Arrays.stream(payload.getClass().getDeclaredFields()).forEach(field -> {
                try {
                    boolean wasAccessible = Modifier.isPrivate(field.getModifiers());
                    field.setAccessible(true);
                    if (!Modifier.isFinal(field.getModifiers()))
                        field.set(payload, field.get(executionResult));
                    field.setAccessible(wasAccessible);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        }

        private boolean isSuccessful() {
            return e == null;
        }

        private Exception getException() {
            return e;
        }
    }
}
