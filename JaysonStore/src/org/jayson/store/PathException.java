package org.jayson.store;

/**
 *
 * @author tofarr
 */
public class PathException extends RuntimeException {

    public PathException(String message) {
        super(message);
    }

    public PathException(String message, Throwable cause) {
        super(message, cause);
    }

    public PathException(Throwable cause) {
        super(cause);
    }

}
