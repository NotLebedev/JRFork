package org.notlebedev;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;

public class FullObjectDump implements Serializable {

    private final String name;
    private final byte[] bytecode;
    private final byte[] objectData;

    public FullObjectDump(Serializable object) throws IOException {
        this.name = object.getClass().getCanonicalName();

        URL url = object.getClass().getResource(object.getClass().getSimpleName() + ".class");
        bytecode = url.openStream().readAllBytes();

        var bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(object);
        out.flush();
        objectData = bos.toByteArray();
        out.close();
        bos.close();
    }

    public byte[] getBytecode() {
        return bytecode;
    }

    public byte[] getObjectData() {
        return objectData;
    }

    public String getName() {
        return name;
    }
}
