package org.notlebedev.networking;

import org.notlebedev.networking.messages.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;

public class SocketMasterConnection implements MasterConnection {
    private final StringSender out;
    private final StringReceiver in;

    public SocketMasterConnection(InetAddress slaveAddress, int slavePort, int inPort) throws IOException {
        out = new StringSender(slaveAddress, slavePort);
        sendString((new ConnectionRequestMessage(inPort)).toJSON().toString());

        in = new StringReceiver(inPort);
        AbstractMessage message = JSONMessageHolder.parseJSONMessage(getString()).toAbstractMessage();
        if (!(message instanceof ConnectionEstablishedMessage))
            throw new IOException();

        setTimeout(0);
    }

    private void sendString(String string) throws IOException {
        out.sendData(string);
    }

    private String getString() throws IOException {
        return in.getData();
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
    public AbstractMessage sendRequest(AbstractMessage request) throws IOException {
        sendString(request.toJSON().toString());

        JSONMessageHolder response = JSONMessageHolder.parseJSONMessage(getString());
        return response.toAbstractMessage();
    }

    @Override
    public void close() throws IOException {
        in.close();
        out.close();
    }
}
