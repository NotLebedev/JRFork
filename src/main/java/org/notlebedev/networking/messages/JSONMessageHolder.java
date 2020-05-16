package org.notlebedev.networking.messages;

import java.util.function.Function;

public class JSONMessageHolder {
    private MessageType messageType;
    private int port;

    void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    void setPort(int port) {
        this.port = port;
    }

    public AbstractMessage toAbstractMessage() {
        return messageType.toAbstractMessageFunction.apply(this);
    }

    private enum MessageType {
        ConnectionRequest(jsonMessageHolder -> new AbstractConnectionRequest(jsonMessageHolder.port)),
        ConnectionEstablished(jsonMessageHolder -> new AbstractConnectionEstablished());

        private final Function<JSONMessageHolder, AbstractMessage> toAbstractMessageFunction;

        MessageType(Function<JSONMessageHolder, AbstractMessage> toAbstractMessageFunction) {
            this.toAbstractMessageFunction = toAbstractMessageFunction;
        }

    }
}
