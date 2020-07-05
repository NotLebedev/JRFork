package org.notlebedev.networking;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

@Deprecated
class ByteSender {
    private final Socket socket;
    private final DataOutputStream out;

    public ByteSender(InetAddress remoteAddress, int port) throws IOException {
        socket = new Socket(remoteAddress, port);
        out  = new DataOutputStream(socket.getOutputStream());
    }

    public void sendData(byte[] message) throws IOException {
        out.write(message, 0, message.length);
    }

    public void close() throws IOException {
        out.close();
        socket.close();
    }
}
