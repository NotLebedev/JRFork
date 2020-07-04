package org.notlebedev.networking.messages;

/**
 * Message sent to establish connection from slave to master
 */
public class ConnectionRequestMessage implements AbstractMessage {
    private final int port;

    public ConnectionRequestMessage(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    @Override
    public JSONMessageHolder toJSON() {
        var json = new JSONMessageHolder();
        json.setMessageType(JSONMessageHolder.MessageType.ConnectionRequest);
        json.setPort(port);
        return json;
    }
}
