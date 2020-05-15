package i.lion.example.slave;

import i.lion.ByteReceiver;

import java.io.IOException;

public class Main {

    public static void main (String []args) {
        try {
            ByteReceiver receiver = new ByteReceiver(4040);
            System.out.println(new String(receiver.getData()));
            receiver.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
