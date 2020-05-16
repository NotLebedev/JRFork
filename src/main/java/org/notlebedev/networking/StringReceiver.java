package org.notlebedev.networking;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class StringReceiver {
    private final ServerSocket socket;
    private final Socket connection;
    private final BufferedReader  in;

    public StringReceiver(int port) throws IOException {
        socket = new ServerSocket(port);
        connection = socket.accept();
        in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    }

    public InetAddress getAddress() {
        return connection.getInetAddress();
    }

    public String getData() throws IOException {
        return in.readLine();
    }

    public void close() throws IOException {
        in.close();
        connection.close();
        socket.close();
    }
}
