package org.notlebedev;

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
            /*ByteReceiver receiver = new ByteReceiver(4040);

            ByteArrayInputStream bis = new ByteArrayInputStream(receiver.getData());
            ObjectInput in = new ObjectInputStream(bis);
            Object o = in.readObject();
            bis.close();
            in.close();
            if (!(o instanceof FullObjectDump))
                return;

            FullObjectDump dump = (FullObjectDump) o;
            cll.addClass(dump.getName(), dump.getBytecode());
            bis = new ByteArrayInputStream(dump.getObjectData());
            in = new CustomClassLoaderObjectInputStream(bis, cll);

            o = in.readObject();

            if (o instanceof TestInterface) {
                ((TestInterface) o).printData();
                System.out.println(((TestInterface) o).square(12));
            } else {
                System.out.println("Not ritght");
            }

            receiver.close();*/
            SlaveConnection connection = new SocketSlaveConnection(4040);
            AbstractMessage message = connection.listenRequest();
            if(message instanceof GetExecutionContextMessage) {
                List<String> context = new ArrayList<>((new ExecutionContext(inst)).getExtraLoadedClassNames());
                var response = new SendExecutionContextMessage(context);
                connection.sendResponse(response);
                message = connection.listenRequest();
                if(message instanceof LoadClassesMessage) {
                    ((LoadClassesMessage) message).getClassBytecodes().forEach(cll::addClass);
                }
                connection.sendResponse(new ConnectionEstablishedMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
