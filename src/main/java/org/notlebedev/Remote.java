package org.notlebedev;

import java.io.Serializable;

/**
 * Runnable + Serializable interface for remote execution of objects via
 * {@link RemoteThread}
 */
public interface Remote extends Runnable, Serializable {
}
