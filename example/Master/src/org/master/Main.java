package org.master;

import org.notlebedev.ClassIntrospection;
import org.notlebedev.InstrumentationHook;
import org.notlebedev.SyntheticClassException;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.HashSet;

public class Main {

    public static void main (String []args) {
        var test = new TestClass("Hello, world!", 1, 2, Arrays.asList(1, 2, 3, 4));
        Instrumentation inst = InstrumentationHook.getInstrumentation();

        try {
            var clazz = test.getClass();
            var intro = new ClassIntrospection(clazz, new HashSet<>());
            System.out.println(intro.getUsedClasses().size());
        } catch (IOException | SyntheticClassException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}