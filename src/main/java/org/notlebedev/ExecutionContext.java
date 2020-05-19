package org.notlebedev;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("rawtypes")
public class ExecutionContext {
    private final Set<Class> loadedClasses;

    public ExecutionContext(Instrumentation instrumentation) {
        loadedClasses = new HashSet<>(instrumentation.getAllLoadedClasses().length);
        loadedClasses.addAll(Arrays.asList(instrumentation.getAllLoadedClasses()));
        loadedClasses.removeIf(Class::isArray);
        loadedClasses.removeIf(Class::isPrimitive);
    }

    /**
     * @return {@link Set} of class names from this execution context, as retrieved by {@code Class::getName} function
     */
    public Set<String> getLoadedClassNames() {
        return loadedClasses.stream().map(Class::getName).collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Get all classes from execution context that are not present in list
     * @param presentClassNames {@link List} of full class names
     * @return {@link Set} of classes
     */
    public Set<Class> getDifference(List<String> presentClassNames) {
        Set<Class> duplicate = new HashSet<>(loadedClasses);
        duplicate.removeIf(clazz -> presentClassNames.contains(clazz.getName()));
        return duplicate;
    }

    public static Map<String, byte[]> toBytecodes(Set<Class> classes) throws IOException {
        var result = new HashMap<String, byte[]>(classes.size());
        for (Class clazz : classes) {
            byte[] bytecode = clazz.getResource(clazz.getSimpleName() + ".class").openStream().readAllBytes();
            result.put(clazz.getName(), bytecode);
        }
        return result;
    }
}
