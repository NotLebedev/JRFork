package i.lion;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class ByteSender {
    private final Socket socket;
    private final DataOutputStream out;

    public ByteSender(InetAddress remoteAddress) throws IOException {
        socket = new Socket(remoteAddress, 4040);
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
