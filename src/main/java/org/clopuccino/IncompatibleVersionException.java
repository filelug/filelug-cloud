package org.clopuccino;

/**
 * <code></code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class IncompatibleVersionException extends Exception {
    public IncompatibleVersionException() {
    }

    public IncompatibleVersionException(Throwable cause) {
        super(cause);
    }

    public IncompatibleVersionException(String message) {
        super(message);
    }

    public IncompatibleVersionException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncompatibleVersionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
