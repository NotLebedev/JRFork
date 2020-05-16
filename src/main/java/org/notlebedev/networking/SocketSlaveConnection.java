package org.notlebedev.networking;

import org.notlebedev.networking.messages.JSONMessageFactory;
import org.notlebedev.networking.messages.JSONMessageHolder;
import org.notlebedev.networking.messages.JSONMessageParser;

import java.io.IOException;

public class SocketSlaveConnection implements SlaveConnection {
    private final ByteSender out;
    private final ByteReceiver in;
    private final JSONMessageFactory messageFactory;
    private final JSONMessageParser messageParser;


    public SocketSlaveConnection(int port) throws IOException {
        messageFactory = new JSONMessageFactory();
        messageParser = new JSONMessageParser();
        in = new ByteReceiver(port);
        JSONMessageHolder request = messageParser.parseJSONMessage(getString());
        if (request.getMessageType() != JSONMessageHolder.MessageType.ConnectionRequest)
            throw new IOException();

        out = new ByteSender(in.getAddress(), request.getPort());
        sendString(messageFactory.buildConnectionEstablishedMessage());
    }

    private void sendString(String string) throws IOException {
        out.sendData(string.getBytes());
    }

    private String getString() throws IOException {
        return new String(in.getData());
    }

    @Override
    public String listenRequest() {
        return null;
    }

    @Override
    public void sendResponse(JSONMessageHolder message) {

    }

    @Override
    public void close() {

    }
}
