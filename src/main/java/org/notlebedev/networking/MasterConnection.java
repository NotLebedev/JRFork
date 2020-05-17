package org.notlebedev.networking;

import org.notlebedev.networking.messages.AbstractMessage;

public interface MasterConnection {

    void setTimeout(int timeout);
    String sendRequest(AbstractMessage request);
    void close();

}
