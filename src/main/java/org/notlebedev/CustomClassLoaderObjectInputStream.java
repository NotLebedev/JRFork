package org.notlebedev;

import java.io.*;

/**
 * CustomClassLoaderObjectInputStream deserializes primitive types and objects
 * previously written by {@link ObjectOutputStream} with the same behaviour as
 * {@link ObjectInputStream}, except it uses class loader specified instead of
 * system class loader
 */
public class CustomClassLoaderObjectInputStream extends ObjectInputStream {

    private final ClassLoader classLoader;

    public CustomClassLoaderObjectInputStream(InputStream inputStream, ClassLoader classLoader) throws IOException {
        super(inputStream);
        this.classLoader = classLoader;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass objectStreamClass) throws ClassNotFoundException {
        return Class.forName(objectStreamClass.getName(), false, classLoader);
    }
}
