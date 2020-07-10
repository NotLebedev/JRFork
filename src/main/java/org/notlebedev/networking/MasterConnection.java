package org.notlebedev.networking;

import org.notlebedev.networking.messages.AbstractMessage;

import java.io.IOException;
import java.net.SocketException;

public interface MasterConnection {
    void setTimeout(int timeout) throws SocketException;

    AbstractMessage sendRequest(AbstractMessage request) throws IOException;

    void close() throws IOException;
}
