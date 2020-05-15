package i.lion.example.slave;

import i.lion.ByteReceiver;

import java.io.IOException;
import java.net.InetAddress;

public class Main {

    public static void main (String []args) {
        try {
            ByteReceiver receiver = new ByteReceiver(InetAddress.getLocalHost());
            System.out.println(new String(receiver.getData()));
            receiver.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
