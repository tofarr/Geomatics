package org.geomatics.geom.io;

/**
 *
 * @author tofar
 */
public class GeomIOException extends RuntimeException {

    public GeomIOException(String message) {
        super(message);
    }

    public GeomIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public GeomIOException(Throwable cause) {
        super(cause);
    }

}
