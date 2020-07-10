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
        if (!(request instanceof ConnectionRequestMessage))
            throw new IOException();

        out = new StringSender(in.getAddress(), ((ConnectionRequestMessage) request).getPort());
        sendString((new ConnectionEstablishedMessage()).toJSON().toString());

        setTimeout(0);
    }

    private void sendString(String string) throws IOException {
        out.sendData(string);
    }

    private String getString() throws IOException {
        return in.getData();
    }

    @Override
    public AbstractMessage listenRequest() throws IOException {
        JSONMessageHolder jsonMessage = JSONMessageHolder.parseJSONMessage(getString());
        return jsonMessage.toAbstractMessage();
    }

    @Override
    public void sendResponse(AbstractMessage message) throws IOException {
        sendString(message.toJSON().toString());
    }

    /**
     * Set timeout for all ingoing and outgoing networking operations, by default timeout is infinite
     *
     * @param timeout non-negative timeout or zero for infinite timeout
     * @throws SocketException connection was in incorrect state and thus could not be modified
     */
    @Override
    public void setTimeout(int timeout) throws SocketException {
        in.setTimeout(timeout);
        out.setTimeout(timeout);
    }

    @Override
    public void close() throws IOException {
        in.close();
        out.close();
    }
}
