package org.notlebedev.networking;

import org.notlebedev.networking.messages.*;

import java.io.IOException;

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

    @Override
    public void close() {

    }
}
