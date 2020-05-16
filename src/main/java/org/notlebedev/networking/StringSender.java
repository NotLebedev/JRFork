package org.notlebedev.networking;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

public class StringSender {
    private final Socket socket;
    private final OutputStreamWriter out;

    public StringSender(InetAddress remoteAddress, int port) throws IOException {
        socket = new Socket(remoteAddress, port);
        out  = new OutputStreamWriter(socket.getOutputStream());
    }

    public void sendData(String message) throws IOException {
        out.write(message + '\n');
    }

    public void close() throws IOException {
        out.close();
        socket.close();
    }
}
