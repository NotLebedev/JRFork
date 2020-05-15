package org.notlebedev;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class ByteArrayClassLoader extends URLClassLoader {

    private final Map<String, byte[]> extraClasses;

    public ByteArrayClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.extraClasses = new HashMap<>();
    }

    public void addClass (String name, byte[] bytecode) {
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
