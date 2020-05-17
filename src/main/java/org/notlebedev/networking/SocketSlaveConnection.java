package org.notlebedev.networking;

import org.notlebedev.networking.messages.*;

import java.io.IOException;
import java.net.SocketException;

public class SocketSlaveConnection implements SlaveConnection {
    private final StringSender out;
    private final StringReceiver in;

    public SocketSlaveConnection(int port) throws IOException {
        in = new StringReceiver(port);
        AbstractMessage request = JSONMessageHolder.parseJSONMessage(getString()).toAbstractMessage();
        if (!(request instanceof AbstractConnectionRequest))
            throw new IOException();

        out = new StringSender(in.getAddress(), ((AbstractConnectionRequest) request).getPort());
        sendString((new AbstractConnectionEstablished()).toJSON().toString());

        setTimeout(0);
    }

    private void sendString(String string) throws IOException {
        out.sendData(string);
    }

    private String getString() throws IOException {
        return in.getData();
    }

    @Override
    public AbstractMessage listenRequest() {
        return null;
    }

    @Override
    public void sendResponse(AbstractMessage message) {

    }

    /**
     * Set timeout for all ingoing and outgoing networking operations, by default timeout is infinite
     * @param timeout non-negative timeout or zero for infinite timeout
     * @throws SocketException connection was in incorrect state and thus could not be modified
     */
    @Override
    public void setTimeout(int timeout) throws SocketException {
        in.setTimeout(timeout);
        out.setTimeout(timeout);
    }

    @Override
    public void close() {

    }
}
