package org.notlebedev.networking.messages;

public class ObjectIsNotRunnableMessage implements ErrorMessage {
    @Override
    public String getMessage() {
        return "Object requested for execution did not implement Runnable interface";
    }

    @Override
    public JSONMessageHolder toJSON() {
        var json = new JSONMessageHolder();
        json.setMessageType(JSONMessageHolder.MessageType.ObjectIsNotRunnable);
        return json;
    }
}
