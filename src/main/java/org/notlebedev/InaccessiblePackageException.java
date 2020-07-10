package org.notlebedev;

import java.lang.reflect.InaccessibleObjectException;

public class InaccessiblePackageException extends Exception {
    private final InaccessibleObjectException cause;
    private final String module;
    private final String pack;

    public InaccessiblePackageException(InaccessibleObjectException cause, String module, String pack) {
        this.cause = cause;
        this.module = module;
        this.pack = pack;
    }

    @Override
    public synchronized Throwable getCause() {
        return cause;
    }

    @Override
    public String getMessage() {
        return "Unable to access package " + module + "/" + pack;
    }
}
