package org.notlebedev.networking;

import org.notlebedev.networking.messages.*;

import java.io.IOException;
import java.net.InetAddress;

public class SocketMasterConnection implements MasterConnection {
    private final StringSender out;
    private final StringReceiver in;

    public SocketMasterConnection(InetAddress slaveAddress, int slavePort, int inPort) throws IOException {
        out = new StringSender(slaveAddress, slavePort);
        sendString((new AbstractConnectionRequest(inPort)).toJSON().toString());

        in = new StringReceiver(inPort);
        AbstractMessage message = JSONMessageHolder.parseJSONMessage(getString()).toAbstractMessage();
        if (!(message instanceof AbstractConnectionEstablished))
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
