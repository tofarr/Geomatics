package org.geomatics.geom;

/**
 * Exception occuring when reading or writing a geometry
 *
 * @author tofarrell
 */
public class GeomIOException extends RuntimeException {

    public GeomIOException(String message) {
        super(message);
    }

    public GeomIOException(String message, Throwable cause) {
        super(message, cause);
    }

}
