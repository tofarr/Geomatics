
package org.jayson.store.criteria;

/**
 *
 * @author tofarr
 */
public class CriteriaException extends RuntimeException {

    public CriteriaException(String message) {
        super(message);
    }

    public CriteriaException(String message, Throwable cause) {
        super(message, cause);
    }

    public CriteriaException(Throwable cause) {
        super(cause);
    }
    
}
