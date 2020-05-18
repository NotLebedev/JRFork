package org.notlebedev.networking.messages;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.function.Function;

public class JSONMessageHolder {
    private MessageType messageType;
    private int port;
    private String[] classNames;

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

    private static class LazyHolder {
        public static final Gson gson = new Gson();
    }

    public static JSONMessageHolder parseJSONMessage(String message) {
        return LazyHolder.gson.fromJson(message, JSONMessageHolder.class);
    }

    @Override
    public String toString() {
        return LazyHolder.gson.toJson(this, this.getClass());
    }

    enum MessageType {
        ConnectionRequest(jsonMessageHolder -> new ConnectionRequestMessage(jsonMessageHolder.port)),
        ConnectionEstablished(jsonMessageHolder -> new ConnectionEstablishedMessage()),
        GetExecutionContext(jsonMessageHolder -> new GetExecutionContextMessage()),
        SendExecutionContext(jsonMessageHolder ->
                new SendExecutionContextMessage(Arrays.asList(jsonMessageHolder.classNames)));

        private final Function<JSONMessageHolder, AbstractMessage> toAbstractMessageFunction;

        MessageType(Function<JSONMessageHolder, AbstractMessage> toAbstractMessageFunction) {
            this.toAbstractMessageFunction = toAbstractMessageFunction;
        }

    }
}
