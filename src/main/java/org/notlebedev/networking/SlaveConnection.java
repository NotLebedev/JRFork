package org.notlebedev.networking;

import org.notlebedev.networking.messages.JSONMessageHolder;

public interface SlaveConnection {

    String listenRequest();
    void sendResponse(JSONMessageHolder message);
    void close();

}
