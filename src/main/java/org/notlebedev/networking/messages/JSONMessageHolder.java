package org.notlebedev.networking.messages;

import com.google.gson.Gson;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JSONMessageHolder {
    private MessageType messageType;
    private int port;
    private String[] classNames;
    private Map<String, String> classBytecodes;

    public static final Base64.Encoder encoder = Base64.getEncoder();
    public static final Base64.Decoder decoder = Base64.getDecoder();

    void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    void setPort(int port) {
        this.port = port;
    }

    public AbstractMessage toAbstractMessage() {
        return messageType.toAbstractMessageFunction.apply(this);
    }

    public void setClassNames(String[] classNames) {
        this.classNames = classNames;
    }

    public void setClassBytecodes(Map<String, byte[]> classBytecodes) {
        this.classBytecodes = classBytecodes.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> encoder.encodeToString(e.getValue())));
    }

    private static class LazyGsonHolder {
        public static final Gson gson = new Gson();
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
        OperationSuccessful(jsonMessageHolder -> new OperationSuccessfulMessage());

        private final Function<JSONMessageHolder, AbstractMessage> toAbstractMessageFunction;

        MessageType(Function<JSONMessageHolder, AbstractMessage> toAbstractMessageFunction) {
            this.toAbstractMessageFunction = toAbstractMessageFunction;
        }

    }
}
