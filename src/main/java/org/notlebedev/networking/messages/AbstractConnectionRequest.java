package org.notlebedev.networking.messages;

public class AbstractConnectionRequest implements AbstractMessage {
    private final int port;

    public AbstractConnectionRequest(int port) {
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
