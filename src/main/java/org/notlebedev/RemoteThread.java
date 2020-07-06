package org.notlebedev;

import org.notlebedev.exceptions.OperationFailedException;
import org.notlebedev.introspection.ObjectIntrospection;
import org.notlebedev.introspection.SyntheticClassException;
import org.notlebedev.networking.MasterConnection;
import org.notlebedev.networking.messages.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class RemoteThread {
    private final MasterConnection connection;
    private final Runnable payload;
    private final Operation operation;

    public RemoteThread(MasterConnection masterConnection, Runnable payload) throws SyntheticClassException {
        this.connection = masterConnection;
        this.payload = payload;
        if(payload.getClass().isSynthetic())
            throw new SyntheticClassException();
        operation = new Operation();
    }

    public void start() {
        operation.start();
    }

    public void join() throws InterruptedException {
        operation.join();
    }

    public boolean isSuccessful() {
        return operation.isSuccessful();
    }

    public Exception getException() {
        return operation.getException();
    }

    public Runnable getPayload() {
        return payload;
    }

    private class Operation extends Thread {
        private Exception e;

        @Override
        public void run() {
            try {
                exec();
            } catch (OperationFailedException | IOException | ClassNotFoundException e) {
                this.e = e;
            }
        }

        private void exec() throws IOException, ClassNotFoundException, OperationFailedException {
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

            ObjectIntrospection objectIntrospection;

            try {
                objectIntrospection = new ObjectIntrospection(payload, omitClasses);
            } catch (SyntheticClassException e) {
                throw new IllegalStateException("Class was expected to be non-synthetic");
            }

            response = connection.sendRequest(new LoadClassesMessage(ExecutionContext
                    .toBytecodes(objectIntrospection.getClassesUsed())));
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

            Arrays.stream(payload.getClass().getFields()).forEach(field -> {
                try {
                    if (!Modifier.isFinal(field.getModifiers()))
                        field.set(payload, field.get(executionResult));
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
