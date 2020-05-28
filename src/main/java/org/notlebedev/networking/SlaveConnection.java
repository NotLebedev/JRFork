package org.notlebedev.networking;

import org.notlebedev.networking.messages.AbstractMessage;

import java.io.IOException;
import java.net.SocketException;

public interface SlaveConnection {

    AbstractMessage listenRequest() throws IOException;
    void sendResponse(AbstractMessage message) throws IOException;
    void setTimeout(int timeout) throws SocketException;
    void close() throws IOException;

}
