package org.notlebedev.networking;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

@Deprecated
public class ByteReceiver extends Thread {
    private final ServerSocket socket;
    private final Socket connection;
    private final DataInputStream in;

    public ByteReceiver(int port) throws IOException {
        socket = new ServerSocket(port);
        connection = socket.accept();
        in = new DataInputStream(connection.getInputStream());
    }

    public InetAddress getAddress() {
        return connection.getInetAddress();
    }

    public byte[] getData() throws IOException {
        byte[] message;
        message = in.readAllBytes();
        return message;
    }

    public void close() throws IOException {
        in.close();
        connection.close();
        socket.close();
    }
}
