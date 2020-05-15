package i.lion;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ByteReceiver extends Thread {
    private final ServerSocket socket;
    private final Socket connection;
    private final DataInputStream in;

    public ByteReceiver(int port) throws IOException {
        socket = new ServerSocket(port);
        connection = socket.accept();
        in = new DataInputStream(connection.getInputStream());
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
