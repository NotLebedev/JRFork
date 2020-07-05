package org.notlebedev.networking.messages;

import java.util.Set;

public class ClassNotFoundMessage implements ErrorMessage {
    private final Set<String> classMissing;

    public ClassNotFoundMessage(Set<String> classMissing) {
        this.classMissing = classMissing;
    }

    public Set<String> getClassMissing() {
        return classMissing;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder("Operation failed, because ");
        sb.append(classMissing.size() > 1 ? "classes" : "class");
        classMissing.forEach(clazz -> sb.append("\n\t").append(clazz));
        return sb.toString();
    }

    @Override
    public JSONMessageHolder toJSON() {
        var json = new JSONMessageHolder();
        json.setMessageType(JSONMessageHolder.MessageType.ClassNotFound);
        json.setClassNames(classMissing.toArray(String[]::new));
        return json;
    }
}
