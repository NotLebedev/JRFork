package org.notlebedev.networking;

import org.notlebedev.networking.messages.AbstractMessage;

public interface SlaveConnection {

    AbstractMessage listenRequest();
    void sendResponse(AbstractMessage message);
    void close();

}
