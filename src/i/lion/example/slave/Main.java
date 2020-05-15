package i.lion.example.slave;

import i.lion.ByteArrayClassLoader;
import i.lion.ByteReceiver;
import i.lion.CustomClassLoaderObjectInputStream;
import i.lion.FullObjectDump;

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
            ByteReceiver receiver = new ByteReceiver(4040);

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

            System.out.println(o.getClass().getConstructors()[0].toString());

            receiver.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
