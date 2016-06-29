package org.om.stream;

import java.io.InputStream;
import java.io.OutputStream;
import org.om.element.ValElement;
import org.om.store.Capabilities;
import org.om.store.StoreException;

/**
 *
 * @author tofar
 */
public interface StreamStore {

    Capabilities getCapabilities();

    StreamHandle get(ValElement key) throws StoreException;

    StreamHandle create() throws StoreException;

    boolean load(StreamHandleProcessor processor) throws StoreException;

    long count() throws StoreException;

    boolean remove(ValElement key) throws StoreException;

    interface StreamHandle {

        ValElement getKey();

        InputStream read() throws StoreException;

        OutputStream write() throws StoreException;
        
        long lastModified();
    }

    interface StreamHandleProcessor {

        boolean process(StreamHandle handle);
    }
}
