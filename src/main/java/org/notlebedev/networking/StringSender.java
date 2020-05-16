package org.notlebedev.networking;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class StringSender {
    private final Socket socket;
    private final DataOutputStream out;

    public StringSender(InetAddress remoteAddress, int port) throws IOException {
        socket = new Socket(remoteAddress, port);
        out  = new DataOutputStream(socket.getOutputStream());
    }

    public void sendData(String message) throws IOException {
        out.writeInt(message.getBytes().length);
        out.write(message.getBytes(), 0, message.getBytes().length);
        out.flush();
    }

    public void close() throws IOException {
        out.close();
        socket.close();
    }
}