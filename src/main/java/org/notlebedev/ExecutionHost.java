package org.notlebedev;

import org.notlebedev.networking.SlaveConnection;
import org.notlebedev.networking.messages.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ExecutionHost implements Runnable {
    private final ArrayList<Object> objectStack;
    private final ByteArrayClassLoader threadClassLoader;
    private final SlaveConnection connection;

    public ExecutionHost(SlaveConnection connection) {
        this.connection = connection;
        objectStack = new ArrayList<>();
        threadClassLoader = new ByteArrayClassLoader(new URL[0], ClassLoader.getSystemClassLoader());
    }

    @Override
    public void run() {
        Instrumentation instrumentation = InstrumentationHook.getInstrumentation();
        Thread.currentThread().setContextClassLoader(threadClassLoader);

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
                try {
                    connection.sendResponse(response);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            } else if (message instanceof LoadClassesMessage) {
                ((LoadClassesMessage) message).getClassBytecodes().forEach((str, bytes) -> System.out.println(str));
                ((LoadClassesMessage) message).getClassBytecodes().forEach(threadClassLoader::addClass);
                try {
                    connection.sendResponse(new OperationSuccessfulMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            } else if (message instanceof SendObjectsMessage) {
                SendObjectsMessage sendObjectsMessage = ((SendObjectsMessage) message);
                ArrayList<Object> objects = new ArrayList<>();
                sendObjectsMessage.getObjects().forEach((name, bytes) -> {
                    var bis = new ByteArrayInputStream(bytes);
                    CustomClassLoaderObjectInputStream objectInputStream;
                    try {
                        objectInputStream = new CustomClassLoaderObjectInputStream(bis, threadClassLoader);
                        objects.add(objectInputStream.readObject());
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                });

                objectStack.addAll(objects);

                try {
                    connection.sendResponse(new OperationSuccessfulMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }
}
