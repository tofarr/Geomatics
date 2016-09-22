package org.ds.data;

import org.ds.StoreException;

/**
 *
 * @author tofarr
 */
public class DataStoreException extends StoreException {

    public DataStoreException(String message) {
        super(message);
    }

    public DataStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataStoreException(Throwable cause) {
        super(cause);
    }

}
