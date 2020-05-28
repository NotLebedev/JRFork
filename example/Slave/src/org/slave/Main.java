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
        try {
            SlaveConnection connection = new SocketSlaveConnection(4040);
            AbstractMessage message = connection.listenRequest();
            if(message instanceof GetExecutionContextMessage) {
                List<String> context = new ArrayList<>((new ExecutionContext(inst)).getExtraLoadedClassNames());
                var response = new SendExecutionContextMessage(context);
                connection.sendResponse(response);
                message = connection.listenRequest();
                if(message instanceof LoadClassesMessage) {
                    ((LoadClassesMessage) message).getClassBytecodes().forEach((str,bytes) -> System.out.println(str));
                    ((LoadClassesMessage) message).getClassBytecodes().forEach(cll::addClass);
                }
                connection.sendResponse(new ConnectionEstablishedMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
