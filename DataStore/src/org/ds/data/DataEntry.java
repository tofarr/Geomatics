package org.ds.data;

import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author tofarr
 */
public interface DataEntry<K> {

    K getKey();

    long getLastModified();
    
    String getHashCode();

    InputStream read() throws DataStoreException;

    OutputStream write() throws DataStoreException;

    void write(InputStream data) throws DataStoreException;
}
