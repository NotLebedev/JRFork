package org.notlebedev.networking.messages;

public class ConnectionEstablishedMessage implements AbstractMessage {
    @Override
    public JSONMessageHolder toJSON() {
        var json = new JSONMessageHolder();
        json.setMessageType(JSONMessageHolder.MessageType.ConnectionEstablished);
        return json;
    }
}
