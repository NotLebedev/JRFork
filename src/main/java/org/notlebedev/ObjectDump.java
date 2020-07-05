package org.notlebedev;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class ObjectDump {

    private final String name;
    private final byte[] objectData;

    public ObjectDump(Object object) throws IOException {
        this.name = object.getClass().getCanonicalName();

        var bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(object);
        out.flush();
        objectData = bos.toByteArray();
        out.close();
        bos.close();
    }

    public byte[] getObjectData() {
        return objectData;
    }

    public String getName() {
        return name;
    }
}
