package org.om.schema;

/**
 *
 * @author tofarrell
 */
public class SchemaException extends RuntimeException {

    private final Path path;

    public SchemaException(Path path, String message) {
        super(message);
        this.path = path;
    }

    public SchemaException(Path path, String message, Throwable cause) {
        super(message, cause);
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public String getMessage() {
        return path.toString() + " : " + super.getMessage(); //To change body of generated methods, choose Tools | Templates.
    }

}
