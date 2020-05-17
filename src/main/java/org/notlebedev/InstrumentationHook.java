package org.notlebedev;

import java.lang.instrument.Instrumentation;
import java.util.UUID;

public class InstrumentationHook {
    public static void premain(String agentArgs, Instrumentation inst) {
        if (agentArgs != null) {
            System.getProperties().put(AGENT_ARGS_KEY, agentArgs);
        }
        System.getProperties().put(INSTRUMENTATION_KEY, inst);
    }

    public static Instrumentation getInstrumentation() {
        return (Instrumentation) System.getProperties().get(INSTRUMENTATION_KEY);
    }

    private static final Object AGENT_ARGS_KEY =
            UUID.fromString("39f6cc50-9079-44e3-9c71-4d60d4b8045e");

    private static final Object INSTRUMENTATION_KEY =
            UUID.fromString("a4f689d1-8c9d-44b7-8cb7-83f49cc94975");
}
