package org.notlebedev;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * This class loader is used to load classes supplied as raw bytecode (as
 * stored in .class files). So bytecode is supposed to be extracted from a
 * .class file with e.g. {@link java.io.InputStream#readAllBytes} or by method
 * {@link ExecutionContext#toBytecodes}
 */
public class ByteArrayClassLoader extends URLClassLoader {
    private final Map<String, byte[]> extraClasses;

    /**
     * Inherit new byte array class loader from an existing one
     *
     * @param parent parental class loader, e.g. system class loader
     */
    public ByteArrayClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
        this.extraClasses = new HashMap<>();
    }

    /**
     * Load a class
     *
     * @param name     class full qualified name in dot format, e.g. java.lang.Integer
     * @param bytecode bytecode of class to be loaded
     */
    public void addClass(String name, byte[] bytecode) {
        extraClasses.put(name, bytecode);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytecode = this.extraClasses.remove(name);
        if (bytecode != null) {
            return defineClass(name, bytecode, 0, bytecode.length);
        }
        return super.findClass(name);
    }
}
