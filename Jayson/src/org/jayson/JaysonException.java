package org.jayson;

/**
 *
 * @author tofar
 */
public class JaysonException extends RuntimeException {

    public JaysonException(String message) {
        super(message);
    }

    public JaysonException(String message, Throwable cause) {
        super(message, cause);
    }

}
