package org.master;

import org.notlebedev.InstrumentationHook;
import org.notlebedev.introspection.ObjectIntrospection;
import org.notlebedev.introspection.SyntheticClassException;
import org.notlebedev.networking.MasterConnection;
import org.notlebedev.networking.SocketMasterConnection;
import org.notlebedev.networking.messages.AbstractMessage;
import org.notlebedev.networking.messages.GetExecutionContextMessage;
import org.notlebedev.networking.messages.SendExecutionContextMessage;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.InetAddress;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static void main (String []args) {
        var test = new TestClass("Hello, world!", 1, 2, Arrays.asList(1, 2.0, 3, new TestInheritedClass()), new Object[]{new TestClass2(), 1.0, 2.0});
        Instrumentation inst = InstrumentationHook.getInstrumentation();

        try {
            MasterConnection connection = new SocketMasterConnection(InetAddress.getLocalHost(), 4040, 8081);
            AbstractMessage response = connection.sendRequest(new GetExecutionContextMessage());
            if(response instanceof SendExecutionContextMessage) {
                SendExecutionContextMessage resp = (SendExecutionContextMessage) response;
                Set<Class<?>> omitClasses = resp.getClassNames().stream().map(s -> {
                    try {
                        return Class.forName(s);
                    } catch (ClassNotFoundException e) {
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toCollection(HashSet::new));
                var oI = new ObjectIntrospection(test, omitClasses);
                System.out.println(oI.getClassesUsed());
            }
        } catch (IOException | SyntheticClassException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}