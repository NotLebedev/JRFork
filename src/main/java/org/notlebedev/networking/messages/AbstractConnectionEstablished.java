package org.notlebedev.networking.messages;

public class AbstractConnectionEstablished implements AbstractMessage {
    @Override
    public JSONMessageHolder toJSON() {
        var json = new JSONMessageHolder();
        json.setMessageType(JSONMessageHolder.MessageType.ConnectionEstablished);
        return json;
    }
}
