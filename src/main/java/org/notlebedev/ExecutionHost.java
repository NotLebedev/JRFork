package org.notlebedev;

import org.notlebedev.networking.SlaveConnection;
import org.notlebedev.networking.messages.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExecutionHost implements Runnable {
    private final ArrayList<Object> objectStack;
    private final ByteArrayClassLoader threadClassLoader;
    private final SlaveConnection connection;
    private Instrumentation instrumentation;

    public ExecutionHost(SlaveConnection connection) {
        this.connection = connection;
        objectStack = new ArrayList<>();
        threadClassLoader = new ByteArrayClassLoader(new URL[0], ClassLoader.getSystemClassLoader());
    }

    @Override
    public void run() {
        instrumentation = InstrumentationHook.getInstrumentation();
        Thread.currentThread().setContextClassLoader(threadClassLoader);

        try {
            control();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void control() throws IOException, ClassNotFoundException {
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
                ((LoadClassesMessage) message).getClassBytecodes().forEach((str, bytes) -> System.out.println(str));
                ((LoadClassesMessage) message).getClassBytecodes().forEach(threadClassLoader::addClass);
                connection.sendResponse(new OperationSuccessfulMessage());
            } else if (message instanceof SendObjectsMessage) {
                SendObjectsMessage sendObjectsMessage = ((SendObjectsMessage) message);
                ArrayList<Object> objects = new ArrayList<>();

                final IOException[] ioException = new IOException[1];
                Set<ClassNotFoundException> classNotFoundExceptions = new HashSet<>();
                sendObjectsMessage.getObjects().forEach((name, bytes) -> {
                    var bis = new ByteArrayInputStream(bytes);
                    CustomClassLoaderObjectInputStream objectInputStream;
                    try {
                        objectInputStream = new CustomClassLoaderObjectInputStream(bis, threadClassLoader);
                        objects.add(objectInputStream.readObject());
                    } catch (IOException e) {
                        ioException[0] = e;
                    } catch (ClassNotFoundException e) {
                        classNotFoundExceptions.add(e);
                    }
                });
                if(ioException[0] != null)
                    throw ioException[0];
                if(!classNotFoundExceptions.isEmpty()) {
                    //connection.sendResponse();
                }

                objectStack.addAll(objects);

                connection.sendResponse(new OperationSuccessfulMessage());
            } else if (message instanceof ExecuteRunnableMessage) {
                if(!(objectStack.get(objectStack.size() - 1) instanceof Runnable)) {
                    connection.sendResponse(new ObjectIsNotRunnableMessage());
                }

                ((Runnable) objectStack.get(objectStack.size() - 1)).run();
                connection.sendResponse(new OperationSuccessfulMessage());
            }
        }
    }
}
