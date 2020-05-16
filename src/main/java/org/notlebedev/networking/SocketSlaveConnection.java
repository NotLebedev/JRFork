package org.notlebedev.networking;

import org.notlebedev.networking.messages.JSONMessageFactory;
import org.notlebedev.networking.messages.JSONMessageHolder;
import org.notlebedev.networking.messages.JSONMessageParser;

import java.io.IOException;

public class SocketSlaveConnection implements SlaveConnection {
    private final StringSender out;
    private final StringReceiver in;
    private final JSONMessageFactory messageFactory;
    private final JSONMessageParser messageParser;


    public SocketSlaveConnection(int port) throws IOException {
        messageFactory = new JSONMessageFactory();
        messageParser = new JSONMessageParser();
        in = new StringReceiver(port);
        JSONMessageHolder request = messageParser.parseJSONMessage(getString());
        if (request.getMessageType() != JSONMessageHolder.MessageType.ConnectionRequest)
            throw new IOException();

        out = new StringSender(in.getAddress(), request.getPort());
        sendString(messageFactory.buildConnectionEstablishedMessage());
    }

    private void sendString(String string) throws IOException {
        out.sendData(string);
    }

    private String getString() throws IOException {
        return in.getData();
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
