package org.notlebedev.networking.messages;

public class GetExecutionContextMessage implements AbstractMessage {
    @Override
    public JSONMessageHolder toJSON() {
        var json = new JSONMessageHolder();
        json.setMessageType(JSONMessageHolder.MessageType.GetExecutionContext);
        return json;
    }
}
