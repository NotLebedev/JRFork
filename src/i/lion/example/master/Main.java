package i.lion.example.master;

import i.lion.ByteSender;

import java.io.IOException;
import java.net.InetAddress;

public class Main {

    public static void main (String []args) {
        String str = "Hello, World!";

        try {
            ByteSender sender = new ByteSender(InetAddress.getLocalHost());
            sender.sendData(str.getBytes());
            sender.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
