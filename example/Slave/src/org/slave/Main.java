package org.slave;

import org.notlebedev.ExecutionHost;
import org.notlebedev.networking.SlaveConnection;
import org.notlebedev.networking.SocketSlaveConnection;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        SlaveConnection connection;
        try {
            connection = new SocketSlaveConnection(4040);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        var host = new ExecutionHost(connection);
        host.run();
    }

}
