package org.rdb;

/**
 *
 * @author tofarr
 */
public class NodeException extends RuntimeException {

    public NodeException(String message) {
        super(message);
    }

    public NodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public NodeException(Throwable cause) {
        super(cause);
    }
    
}
