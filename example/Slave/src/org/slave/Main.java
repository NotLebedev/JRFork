package org.slave;

import org.notlebedev.ExecutionHost;
import org.notlebedev.networking.SlaveConnection;
import org.notlebedev.networking.SocketSlaveConnection;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        int port = 4040;
        if(args.length > 0 && args[0].equals("--port"))
            port = new Scanner(args[1]).nextInt();

        SlaveConnection connection;
        try {
            connection = new SocketSlaveConnection(port);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        var host = new ExecutionHost(connection);
        host.run();
    }

}
