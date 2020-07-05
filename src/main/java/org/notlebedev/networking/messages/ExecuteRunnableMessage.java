package org.notlebedev.networking.messages;

/**
 * Order slave to execute a {@link Runnable} object
 * In case of {@link org.notlebedev.ExecutionContext} this will test an object at
 * to of stack if it is a {@link Runnable} and if it is will will trigger run
 * method
 */
public class ExecuteRunnableMessage implements AbstractMessage {
    @Override
    public JSONMessageHolder toJSON() {
        var json = new JSONMessageHolder();
        json.setMessageType(JSONMessageHolder.MessageType.ExecuteRunnable);
        return json;
    }
}
