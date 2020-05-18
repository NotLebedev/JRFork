package org.notlebedev.networking.messages;

import java.util.List;

public class SendExecutionContextMessage implements AbstractMessage {
    private final List<String> classNames;

    public SendExecutionContextMessage(List<String> classNames) {
        this.classNames = classNames;
    }

    public List<String> getClassNames() {
        return classNames;
    }

    @Override
    public JSONMessageHolder toJSON() {
        var json = new JSONMessageHolder();
        json.setMessageType(JSONMessageHolder.MessageType.SendExecutionContext);
        json.setClassNames(classNames.toArray(String[]::new));
        return json;
    }
}
