package org.notlebedev.networking.messages;

public class GetObjectsMessage implements AbstractMessage {
    private final Integer objectsToGet;

    public GetObjectsMessage(Integer objectsToGet) {
        this.objectsToGet = objectsToGet;
    }

    @Override
    public JSONMessageHolder toJSON() {
        var json = new JSONMessageHolder();
        json.setMessageType(JSONMessageHolder.MessageType.GetObjects);
        json.setObjectsToGet(objectsToGet);
        return json;
    }
}
