package org.om.schema;

/**
 *
 * @author tofar
 */
public class ValidationResult {
    
    public static final ValidationResult SUCCESS = new ValidationResult(true, null);

    private final boolean success;
    private final String msg;

    public ValidationResult(boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }
    
    public boolean isSuccess() {
        return success;
    }

    public String getMsg() {
        return msg;
    }
}
