package org.master;

import org.notlebedev.RemoteThread;
import org.notlebedev.introspection.exceptions.SyntheticClassException;
import org.notlebedev.networking.MasterConnection;
import org.notlebedev.networking.SocketMasterConnection;

import java.io.IOException;
import java.net.InetAddress;

public class Main {

    public static void main(String[] args) {
        try {
            MasterConnection connection = new SocketMasterConnection(InetAddress.getLocalHost(), 4040, 8081);

            Addition add = new Addition(new Integer[]{1, 2, 3, 4, 5, 6});

            RemoteThread rt = new RemoteThread(connection, add);
            rt.start();

            System.out.println("Thread is running remotely");

            rt.join();
            if (!rt.isSuccessful()) {
                rt.getException().printStackTrace();
                return;
            }

            System.out.println(add.getSum());
        } catch (IOException | SyntheticClassException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}