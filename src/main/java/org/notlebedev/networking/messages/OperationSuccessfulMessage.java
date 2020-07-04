package org.notlebedev.networking.messages;

/**
 * Generic response equal to OK
 */
public class OperationSuccessfulMessage implements AbstractMessage {
    @Override
    public JSONMessageHolder toJSON() {
        var json = new JSONMessageHolder();
        json.setMessageType(JSONMessageHolder.MessageType.OperationSuccessful);
        return json;
    }
}
