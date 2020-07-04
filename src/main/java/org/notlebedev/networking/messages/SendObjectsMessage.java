package org.notlebedev.networking.messages;

import java.util.Map;

public class SendObjectsMessage implements AbstractMessage {
    private final Map<String, byte[]> objects;

    public SendObjectsMessage(Map<String, byte[]> objects) {
        this.objects = objects;
    }

    public Map<String, byte[]> getObjects() {
        return objects;
    }

    @Override
    public JSONMessageHolder toJSON() {
        var json = new JSONMessageHolder();
        json.setMessageType(JSONMessageHolder.MessageType.SendObjects);
        json.setObjects(objects);
        return json;
    }
}
