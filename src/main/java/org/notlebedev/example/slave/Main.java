package org.notlebedev.example.slave;

import org.notlebedev.ByteArrayClassLoader;
import org.notlebedev.networking.ByteReceiver;
import org.notlebedev.CustomClassLoaderObjectInputStream;
import org.notlebedev.FullObjectDump;
import org.notlebedev.example.master.TestInterface;
import org.notlebedev.networking.SlaveConnection;
import org.notlebedev.networking.SocketSlaveConnection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.URL;

public class Main {


    public static void main(String[] args) {
        ByteArrayClassLoader cll = new ByteArrayClassLoader(new URL[0], ClassLoader.getSystemClassLoader());
        Thread.currentThread().setContextClassLoader(cll);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
