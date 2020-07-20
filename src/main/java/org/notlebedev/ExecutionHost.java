package org.notlebedev;

import org.notlebedev.introspection.ObjectIntrospection;
import org.notlebedev.networking.SlaveConnection;
import org.notlebedev.networking.messages.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.instrument.Instrumentation;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Example slave that can be used as a basis for a remote execution server.
 * This execution host implements a stack machine on which {@link Serializable}
 * and {@link Remote} objects can be loaded executed (via calling
 * {@link Runnable#run} method)
 * and than retrieved back. This is used in example.Slave project to work in
 * pair with example.Master project and demonstrate work of a
 * {@link RemoteThread}
 */
public class ExecutionHost implements Runnable {
    private final ArrayList<Serializable> objectStack;
    private final ByteArrayClassLoader threadClassLoader;
    private final SlaveConnection connection;
    private Instrumentation instrumentation;

    public static final Logger logger = Logger.getLogger(
            ObjectIntrospection.class.getName());

    /**
     * @param connection fresh new connection to Master
     */
    public ExecutionHost(SlaveConnection connection) {
        this.connection = connection;
        objectStack = new ArrayList<>();
        threadClassLoader = new ByteArrayClassLoader(ClassLoader.getSystemClassLoader());
    }

    @Override
    public void run() {
        instrumentation = InstrumentationHook.getInstrumentation();
        Thread.currentThread().setContextClassLoader(threadClassLoader);

        try {
            control();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void control() throws IOException {
        while (true) {
            AbstractMessage message;
            try {
                message = connection.listenRequest();
            } catch (IOException e) {
                break;
            }
            if (message instanceof GetExecutionContextMessage) {
                List<String> context = new ArrayList<>((new ExecutionContext(instrumentation)).getExtraLoadedClassNames());
                var response = new SendExecutionContextMessage(context);
                connection.sendResponse(response);
            } else if (message instanceof LoadClassesMessage) {
                ((LoadClassesMessage) message).getClassBytecodes().forEach((str, bytes) ->
                        logger.log(Level.FINE, "Loading class " + str));
                ((LoadClassesMessage) message).getClassBytecodes().forEach(threadClassLoader::addClass);
                connection.sendResponse(new OperationSuccessfulMessage());
            } else if (message instanceof SendObjectsMessage) {
                SendObjectsMessage sendObjectsMessage = ((SendObjectsMessage) message);
                ArrayList<Object> objects = new ArrayList<>();

                final IOException[] ioException = new IOException[1];
                Set<String> classNotFoundExceptions = new HashSet<>();
                sendObjectsMessage.getObjects().forEach((name, bytes) -> {
                    //This iterator will try to load all objects provided,
                    //however this might fail due to internal IOException,
                    //which can not be recovered and thus thread must be stopped
                    //or because some classes were not present, then it`s
                    //possible, that master will retry this operation after
                    //providing all required classes
                    var bis = new ByteArrayInputStream(bytes);
                    CustomClassLoaderObjectInputStream objectInputStream;
                    try {
                        objectInputStream = new CustomClassLoaderObjectInputStream(bis, threadClassLoader);
                        objects.add(objectInputStream.readObject());
                    } catch (IOException e) {
                        ioException[0] = e;
                    } catch (ClassNotFoundException e) {
                        classNotFoundExceptions.add(name);
                    }
                });
                if (ioException[0] != null)
                    //This error is not recoverable and process will terminate
                    throw ioException[0];
                if (!classNotFoundExceptions.isEmpty()) {
                    connection.sendResponse(new ClassNotFoundMessage(classNotFoundExceptions));
                    continue;
                }

                objects.forEach(obj -> {
                    if (!(obj instanceof Runnable))
                        throw new IllegalStateException("De serialized objects is" +
                                "expected to implement Serializable");
                    objectStack.add((Serializable) obj);
                });

                connection.sendResponse(new OperationSuccessfulMessage());
            } else if (message instanceof ExecuteRunnableMessage) {
                //Note that execution is possible only for Runnable objects
                //but object of any type can be loaded on stack, this
                if (!(objectStack.get(objectStack.size() - 1) instanceof Runnable)) {
                    connection.sendResponse(new ObjectIsNotRunnableMessage());
                }

                ((Runnable) objectStack.get(objectStack.size() - 1)).run();
                connection.sendResponse(new OperationSuccessfulMessage());
            } else if (message instanceof GetObjectsMessage) {
                int objectsCount = ((GetObjectsMessage) message).getObjectsToGet();

                Map<String, byte[]> objects = new HashMap<>();
                for (int i = 0; i < objectsCount; i++) {
                    var dump = new ObjectDump(objectStack.remove(objectStack.size() - 1));
                    objects.put(dump.getName(), dump.getObjectData());
                }

                connection.sendResponse(new SendObjectsMessage(objects));
            }
        }
    }
}
