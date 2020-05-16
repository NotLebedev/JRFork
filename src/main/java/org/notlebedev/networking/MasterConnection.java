package org.notlebedev.networking;

import org.notlebedev.networking.messages.JSONMessageHolder;

public interface MasterConnection {

    void setTimeout(int timeout);
    String sendRequest(JSONMessageHolder request);
    void close();

}
