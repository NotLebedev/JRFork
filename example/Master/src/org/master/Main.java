package org.master;

import org.notlebedev.InstrumentationHook;
import org.notlebedev.RemoteThread;
import org.notlebedev.exceptions.OperationFailedException;
import org.notlebedev.introspection.SyntheticClassException;
import org.notlebedev.networking.MasterConnection;
import org.notlebedev.networking.SocketMasterConnection;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.InetAddress;
import java.util.*;

public class Main {

    public static void main (String []args) {
        var test = new TestClass("Hello, world!", 1, 2, Arrays.asList(1, 2.0, 3, new TestInheritedClass()), new Object[]{new TestClass2(), 1.0, 2.0});
        Instrumentation inst = InstrumentationHook.getInstrumentation();

        try {
            MasterConnection connection = new SocketMasterConnection(InetAddress.getLocalHost(), 4040, 8081);

            Addition add = new Addition(new Integer[]{1, 2, 3, 4, 5, 6});
            RemoteThread rt = new RemoteThread(connection, add);
            rt.start();

            System.out.println(add.getSum());
        } catch (IOException | SyntheticClassException | ClassNotFoundException | OperationFailedException e) {
            e.printStackTrace();
        }
    }

}