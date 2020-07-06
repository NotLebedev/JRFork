package org.notlebedev;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class can be used to sync loaded classes of two java-machines. It
 * acquires handles of all loaded classes via {@link Instrumentation}, which
 * can be loaded using {@link InstrumentationHook} (therefore
 * InstrumentationProvider jar java agent should be loaded with -javaagent
 * option)
 */
@SuppressWarnings("rawtypes")
public class ExecutionContext {
    private final Set<Class> loadedClasses;

    /**
     * Default java packages that are the same in all ctx
     */
    private final static String[] packageBlackList = {
            "java",
            "sun",
            "javax",
            "jdk",
    };

    public ExecutionContext(Instrumentation instrumentation) {
        loadedClasses = new HashSet<>(instrumentation.getAllLoadedClasses().length);
        loadedClasses.addAll(Arrays.asList(instrumentation.getAllLoadedClasses()));
        loadedClasses.removeIf(Class::isArray);
        loadedClasses.removeIf(Class::isPrimitive);
        Arrays.stream(packageBlackList)
                .forEach(pckg -> loadedClasses.removeIf(clazz -> clazz.getTypeName().startsWith(pckg)));
    }

    /**
     * @return {@link Set} of class names from this execution context, as retrieved by {@code Class::getName} function,
     * excluding default java library classes
     */
    public Set<String> getExtraLoadedClassNames() {
        return loadedClasses.stream().map(Class::getName).collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Get all classes from execution context (except default java library) that are not present in list
     * @param presentClassNames {@link List} of full class names
     * @return {@link Set} of classes
     */
    public Set<Class> getDifference(List<String> presentClassNames) {
        Set<Class> duplicate = new HashSet<>(loadedClasses);
        duplicate.removeIf(clazz -> presentClassNames.contains(clazz.getName()));
        return duplicate;
    }

    /**
     * Get bytecode and names of given classes to later load it with
     * {@link ByteArrayClassLoader} (or use otherwise)
     * @param classes {@link Set} of classes to be loaded
     * @return {@link Map} where key is fully qualified name of class in dot
     * format (e.g. java.lang.Integer) and value is bytecode
     * @throws IOException method failed to open some of the .class files
     */
    public static Map<String, byte[]> toBytecodes(Set<Class<?>> classes) throws IOException {
        var result = new HashMap<String, byte[]>(classes.size());
        for (Class clazz : classes) {
            if (clazz.isSynthetic())
                continue;
            InputStream byteIn = clazz.getResourceAsStream(
                    clazz.getTypeName().replace(clazz.getPackageName() + ".", "") + ".class");
            result.put(clazz.getName(), byteIn.readAllBytes());
        }
        return result;
    }
}
