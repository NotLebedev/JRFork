package org.notlebedev;

import org.notlebedev.networking.MasterConnection;
import org.notlebedev.networking.SocketMasterConnection;
import org.notlebedev.networking.messages.AbstractMessage;
import org.notlebedev.networking.messages.GetExecutionContextMessage;
import org.notlebedev.networking.messages.LoadClassesMessage;
import org.notlebedev.networking.messages.SendExecutionContextMessage;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.InetAddress;
import java.util.Arrays;

public class Main {

    public static void main (String []args) {
        var test = new TestClass("Hello, world!", 1, 2, Arrays.asList(1, 2, 3, 4));
        Instrumentation inst = InstrumentationHook.getInstrumentation();

        try {
            /*ByteSender sender = new ByteSender(InetAddress.getLocalHost(), 4040);

            var bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(new FullObjectDump(test));
            out.flush();
            sender.sendData(bos.toByteArray());

            out.close();
            bos.close();
            sender.close();*/
            MasterConnection connection = new SocketMasterConnection(InetAddress.getLocalHost(), 4040, 8081);
            AbstractMessage response = connection.sendRequest(new GetExecutionContextMessage());
            if(response instanceof SendExecutionContextMessage) {
                SendExecutionContextMessage resp = (SendExecutionContextMessage) response;
                ExecutionContext ctx = new ExecutionContext(inst);
                response = connection.sendRequest(new LoadClassesMessage(ExecutionContext.toBytecodes(ctx.getDifference(resp.getClassNames()))));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}