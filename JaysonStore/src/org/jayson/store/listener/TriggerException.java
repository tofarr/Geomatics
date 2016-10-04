package org.jayson.store.listener;

/**
 *
 * @author tofarr
 */
public class TriggerException extends RuntimeException {

    public TriggerException(String message) {
        super(message);
    }

    public TriggerException(String message, Throwable cause) {
        super(message, cause);
    }

    public TriggerException(Throwable cause) {
        super(cause);
    }

}
