package i.lion;

import java.io.*;

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
