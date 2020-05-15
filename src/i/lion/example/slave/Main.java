package i.lion.example.slave;

import i.lion.ByteReceiver;
import i.lion.FullObjectDump;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

public class Main {

    public static void main (String []args) {
        try {
            ByteReceiver receiver = new ByteReceiver(4040);

            ByteArrayInputStream bis = new ByteArrayInputStream(receiver.getData());
            ObjectInput in = new ObjectInputStream(bis);
            Object o = in.readObject();
            if (!(o instanceof FullObjectDump))
                return;

            FullObjectDump dump = (FullObjectDump) o;
            receiver.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
