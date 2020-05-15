package i.lion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;

public class FullObjectDump<T extends Serializable> {

    private final T object;
    private final byte[] bytecode;
    private final byte[] objectData;

    public FullObjectDump(T object) throws IOException {
        this.object = object;

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
}
