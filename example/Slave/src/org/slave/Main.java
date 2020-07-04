package org.slave;

import org.notlebedev.ByteArrayClassLoader;
import org.notlebedev.ExecutionContext;
import org.notlebedev.InstrumentationHook;
import org.notlebedev.networking.SlaveConnection;
import org.notlebedev.networking.SocketSlaveConnection;
import org.notlebedev.networking.messages.*;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Main {


    public static void main(String[] args) {
        ByteArrayClassLoader cll = new ByteArrayClassLoader(new URL[0], ClassLoader.getSystemClassLoader());
        Thread.currentThread().setContextClassLoader(cll);

        Instrumentation inst = InstrumentationHook.getInstrumentation();
        SlaveConnection connection;
        try {
            connection = new SocketSlaveConnection(4040);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        while (true) {
            AbstractMessage message;
            try {
                message = connection.listenRequest();
            } catch (IOException e) {
                break;
            }
            if (message instanceof GetExecutionContextMessage) {
                List<String> context = new ArrayList<>((new ExecutionContext(inst)).getExtraLoadedClassNames());
                var response = new SendExecutionContextMessage(context);
                try {
                    connection.sendResponse(response);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            } else if (message instanceof LoadClassesMessage) {
                ((LoadClassesMessage) message).getClassBytecodes().forEach((str, bytes) -> System.out.println(str));
                ((LoadClassesMessage) message).getClassBytecodes().forEach(cll::addClass);
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
