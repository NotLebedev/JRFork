package org.notlebedev;

import org.notlebedev.introspection.ObjectIntrospection;
import org.notlebedev.introspection.SyntheticClassException;
import org.notlebedev.networking.MasterConnection;
import org.notlebedev.networking.messages.AbstractMessage;
import org.notlebedev.networking.messages.GetExecutionContextMessage;
import org.notlebedev.networking.messages.LoadClassesMessage;
import org.notlebedev.networking.messages.SendExecutionContextMessage;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class RemoteThread {
    private final MasterConnection connection;
    private Runnable payload;

    public RemoteThread(MasterConnection masterConnection, Runnable payload) throws SyntheticClassException {
        this.connection = masterConnection;
        this.payload = payload;
        if(payload.getClass().isSynthetic())
            throw new SyntheticClassException();
    }

    public void start() throws IOException, ClassNotFoundException {
        AbstractMessage response = connection.sendRequest(new GetExecutionContextMessage());
        if(!(response instanceof SendExecutionContextMessage))
            throw new IOException();
        SendExecutionContextMessage resp = (SendExecutionContextMessage) response;
        Set<Class<?>> omitClasses = resp.getClassNames().stream().map(s -> {
            try {
                return Class.forName(s);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toCollection(HashSet::new));

        ObjectIntrospection objectIntrospection;

        try {
            objectIntrospection = new ObjectIntrospection(payload, omitClasses);
        } catch (SyntheticClassException e) {
            throw new IllegalStateException("Class was expected to be non-synthetic");
        }

        response = connection.sendRequest(new LoadClassesMessage(ExecutionContext
                .toBytecodes(objectIntrospection.getClassesUsed())));
    }

    public Runnable getPayload() {
        return payload;
    }
}
