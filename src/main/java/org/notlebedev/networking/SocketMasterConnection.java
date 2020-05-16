package org.notlebedev.networking;

import org.notlebedev.networking.messages.JSONMessageFactory;
import org.notlebedev.networking.messages.JSONMessageHolder;
import org.notlebedev.networking.messages.JSONMessageParser;

import java.io.IOException;
import java.net.InetAddress;

public class SocketMasterConnection implements MasterConnection {

    private final StringSender out;
    private final StringReceiver in;
    private final JSONMessageFactory messageFactory;
    private final JSONMessageParser messageParser;

    public SocketMasterConnection(InetAddress slaveAddress, int slavePort, int inPort) throws IOException {
        out = new StringSender(slaveAddress, slavePort);
        messageFactory = new JSONMessageFactory();
        messageParser = new JSONMessageParser();
        sendString(messageFactory.buildConnectionRequestMessage(inPort));

        in = new StringReceiver(inPort);
        JSONMessageHolder message = messageParser.parseJSONMessage(getString());
        if (message.getMessageType() != JSONMessageHolder.MessageType.ConnectionEstablished)
            throw new IOException();
    }

    private void sendString(String string) throws IOException {
        out.sendData(string);
    }

    private String getString() throws IOException {
        return in.getData();
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
