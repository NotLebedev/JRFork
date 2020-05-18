package org.notlebedev.networking.messages;

import java.util.Map;

public class LoadClassesMessage implements AbstractMessage {
    private final Map<String, byte[]> classBytecodes;

    public LoadClassesMessage(Map<String, byte[]> classBytecodes) {
        this.classBytecodes = classBytecodes;
    }

    public Map<String, byte[]> getClassBytecodes() {
        return classBytecodes;
    }

    @Override
    public JSONMessageHolder toJSON() {
        var json = new JSONMessageHolder();
        json.setMessageType(JSONMessageHolder.MessageType.LoadClasses);
        json.setClassBytecodes(classBytecodes);
        return json;
    }
}
