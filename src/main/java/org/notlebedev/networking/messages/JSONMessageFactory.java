package org.notlebedev.networking.messages;

import com.google.gson.Gson;

public class JSONMessageFactory {

    private final Gson gson;

    public JSONMessageFactory() {
        gson = new Gson();
    }

    public String buildConnectionRequestMessage(int port) {
        JSONMessageHolder msg = new JSONMessageHolder();
        msg.setMessageType(JSONMessageHolder.MessageType.ConnectionRequest);
        msg.setPort(port);
        return toJson(msg);
    }

    public String buildConnectionEstablishedMessage() {
        JSONMessageHolder msg = new JSONMessageHolder();
        msg.setMessageType(JSONMessageHolder.MessageType.ConnectionEstablished);
        return toJson(msg);
    }

    private String toJson(JSONMessageHolder msg) {
        return gson.toJson(msg, JSONMessageHolder.class);
    }
}
