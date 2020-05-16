package org.notlebedev.networking;

import org.notlebedev.networking.messages.JSONMessageFactory;
import org.notlebedev.networking.messages.JSONMessageHolder;
import org.notlebedev.networking.messages.JSONMessageParser;

import java.io.IOException;
import java.net.InetAddress;

public class SocketMasterConnection implements MasterConnection {

    private final ByteSender out;
    private final ByteReceiver in;
    private final JSONMessageFactory messageFactory;
    private final JSONMessageParser messageParser;

    public SocketMasterConnection(InetAddress slaveAddress, int slavePort, int inPort) throws IOException {
        out = new ByteSender(slaveAddress, slavePort);
        messageFactory = new JSONMessageFactory();
        messageParser = new JSONMessageParser();
        sendString(messageFactory.buildConnectionRequestMessage(inPort));

        in = new ByteReceiver(inPort);
        JSONMessageHolder message = messageParser.parseJSONMessage(getString());
        if (message.getMessageType() != JSONMessageHolder.MessageType.ConnectionEstablished)
            throw new IOException();
    }

    private void sendString(String string) throws IOException {
        out.sendData(string.getBytes());
    }

    private String getString() throws IOException {
        return new String(in.getData());
    }

    @Override
    public void setTimeout(int timeout) {

    }

    @Override
    public String sendRequest(JSONMessageHolder request) {
        return null;
    }

    @Override
    public void close() {

    }
}
