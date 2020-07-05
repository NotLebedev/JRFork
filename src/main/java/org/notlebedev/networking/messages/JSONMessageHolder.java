package org.notlebedev.networking.messages;

import com.google.gson.Gson;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Java object to JSON adapter used to send {@link AbstractMessage} over json
 */
public class JSONMessageHolder {
    private MessageType messageType;
    private int port;
    private String[] classNames;
    private Map<String, String> classBytecodes;
    private Map<String, String> objects;
    private Integer objectsToGet;

    private static final Base64.Encoder encoder = Base64.getEncoder();
    private static final Base64.Decoder decoder = Base64.getDecoder();

    void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    void setPort(int port) {
        this.port = port;
    }

    public AbstractMessage toAbstractMessage() {
        return messageType.toAbstractMessageFunction.apply(this);
    }

    void setClassNames(String[] classNames) {
        this.classNames = classNames;
    }

    void setClassBytecodes(Map<String, byte[]> classBytecodes) {
        this.classBytecodes = classBytecodes.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> encoder.encodeToString(e.getValue())));
    }

    void setObjects(Map<String, byte[]> objects) {
        this.objects = objects.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> encoder.encodeToString(e.getValue())));
    }

    public void setObjectsToGet(Integer objectsToGet) {
        this.objectsToGet = objectsToGet;
    }

    private static class LazyGsonHolder {
        private static final Gson gson = new Gson();
    }

    public static JSONMessageHolder parseJSONMessage(String message) {
        return LazyGsonHolder.gson.fromJson(message, JSONMessageHolder.class);
    }

    @Override
    public String toString() {
        return LazyGsonHolder.gson.toJson(this, this.getClass());
    }

    enum MessageType {
        ConnectionRequest(jsonMessageHolder -> new ConnectionRequestMessage(jsonMessageHolder.port)),
        ConnectionEstablished(jsonMessageHolder -> new ConnectionEstablishedMessage()),
        GetExecutionContext(jsonMessageHolder -> new GetExecutionContextMessage()),
        SendExecutionContext(jsonMessageHolder ->
                new SendExecutionContextMessage(Arrays.asList(jsonMessageHolder.classNames))),
        LoadClasses(jsonMessageHolder -> {
            Map<String, byte[]> converted = jsonMessageHolder.classBytecodes.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> decoder.decode(e.getValue())));
            return new LoadClassesMessage(converted);
        }),
        OperationSuccessful(jsonMessageHolder -> new OperationSuccessfulMessage()),
        SendObjects(jsonMessageHolder -> {
            Map<String, byte[]> converted = jsonMessageHolder.objects.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> decoder.decode(e.getValue())));
            return new SendObjectsMessage(converted);
        }),
        ExecuteRunnable(jsonMessageHolder -> new ExecuteRunnableMessage()),
        ObjectIsNotRunnable(jsonMessageHolder -> new ObjectIsNotRunnableMessage()),
        GetObjects(jsonMessageHolder -> new GetObjectsMessage(jsonMessageHolder.objectsToGet)),
        ClassNotFound(jsonMessageHolder -> new ClassNotFoundMessage(
                new HashSet<>(Arrays.asList(jsonMessageHolder.classNames))
        ));

        private final Function<JSONMessageHolder, AbstractMessage> toAbstractMessageFunction;

        MessageType(Function<JSONMessageHolder, AbstractMessage> toAbstractMessageFunction) {
            this.toAbstractMessageFunction = toAbstractMessageFunction;
        }

    }
}
