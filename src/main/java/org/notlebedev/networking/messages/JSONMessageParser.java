package org.notlebedev.networking.messages;

import com.google.gson.Gson;

public class JSONMessageParser {
    private final Gson gson;

    public JSONMessageParser() {
        gson = new Gson();
    }

    public JSONMessageHolder parseJSONMessage(String message) {
        return gson.fromJson(message, JSONMessageHolder.class);
    }
}
