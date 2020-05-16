package org.notlebedev.networking.messages;

public class JSONMessageHolder {
    private MessageType messageType;
    private int port;

    public MessageType getMessageType() {
        return messageType;
    }

    void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public int getPort() {
        return port;
    }

    void setPort(int port) {
        this.port = port;
    }

    public enum MessageType {
        ConnectionRequest,
        ConnectionEstablished
    }
}
