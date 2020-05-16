package org.notlebedev.example.master;

import org.notlebedev.ByteSender;
import org.notlebedev.FullObjectDump;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.Arrays;

public class Main {

    public static void main (String []args) {
        var test = new TestClass("Hello, world!", 1, 2, Arrays.asList(1, 2, 3, 4));

        try {
            ByteSender sender = new ByteSender(InetAddress.getLocalHost(), 4040);

            var bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(new FullObjectDump(test));
            out.flush();
            sender.sendData(bos.toByteArray());

            out.close();
            bos.close();
            sender.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}