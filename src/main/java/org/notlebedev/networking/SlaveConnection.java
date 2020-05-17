package org.notlebedev.networking;

import org.notlebedev.networking.messages.AbstractMessage;

import java.net.SocketException;

public interface SlaveConnection {

    AbstractMessage listenRequest();
    void sendResponse(AbstractMessage message);
    void setTimeout(int timeout) throws SocketException;
    void close();

}
