package org.notlebedev.networking;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

class StringReceiver {
    private final ServerSocket socket;
    private final Socket connection;
    private final DataInputStream in;

    public StringReceiver(int port) throws IOException {
        socket = new ServerSocket(port);
        connection = socket.accept();
        in = new DataInputStream(connection.getInputStream());
    }

    public InetAddress getAddress() {
        return connection.getInetAddress();
    }

    public String getData() throws IOException {
        int messageSize = in.readInt();
        return new String(in.readNBytes(messageSize));
    }

    public void setTimeout(int timeout) throws SocketException {
        connection.setSoTimeout(timeout);
        socket.setSoTimeout(timeout);
    }

    public void close() throws IOException {
        in.close();
        connection.close();
        socket.close();
    }
}
