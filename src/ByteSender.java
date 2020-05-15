import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class ByteSender extends Thread {

    private final InetAddress remoteAddress;
    private final byte[] message;

    public ByteSender(InetAddress remoteAddress, byte[] message) {
        this.remoteAddress = remoteAddress;
        this.message = message;
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(remoteAddress, 8080);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.write(message);
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
