package org.notlebedev.networking;

import org.notlebedev.networking.messages.AbstractMessage;

import java.io.IOException;

public interface MasterConnection {

    void setTimeout(int timeout);
    AbstractMessage sendRequest(AbstractMessage request) throws IOException;
    void close() throws IOException;

}
