package org.notlebedev;

import org.notlebedev.exceptions.OperationFailedException;
import org.notlebedev.introspection.ObjectIntrospection;
import org.notlebedev.introspection.SyntheticClassException;
import org.notlebedev.networking.MasterConnection;
import org.notlebedev.networking.messages.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RemoteThread {
    private final MasterConnection connection;
    private final Runnable payload;

    public RemoteThread(MasterConnection masterConnection, Runnable payload) throws SyntheticClassException {
        this.connection = masterConnection;
        this.payload = payload;
        if(payload.getClass().isSynthetic())
            throw new SyntheticClassException();
    }

    public void start() throws IOException, ClassNotFoundException, OperationFailedException {
        AbstractMessage response = connection.sendRequest(new GetExecutionContextMessage());
        if(!(response instanceof SendExecutionContextMessage))
            throw new OperationFailedException();
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
        if(!(response instanceof OperationSuccessfulMessage))
            throw new OperationFailedException();

        var objDump = new ObjectDump(payload);
        Map<String, byte[]> obj = new HashMap<>();
        obj.put(objDump.getName(), objDump.getObjectData());
        response = connection.sendRequest(new SendObjectsMessage(obj));
        if(!(response instanceof OperationSuccessfulMessage))
            throw new OperationFailedException();

        response = connection.sendRequest(new ExecuteRunnableMessage());
        if(!(response instanceof OperationSuccessfulMessage))
            throw new OperationFailedException();
    }

    public Runnable getPayload() {
        return payload;
    }
}
