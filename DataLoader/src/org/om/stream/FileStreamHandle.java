package org.om.stream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.om.element.ValElement;
import org.om.store.StoreException;
import org.om.stream.StreamStore.StreamHandle;

/**
 *
 * @author tofar
 */
public class FileStreamHandle implements StreamHandle {

    private final ValElement key;
    private final File file;

    public FileStreamHandle(ValElement key, File file) {
        this.key = key;
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    @Override
    public ValElement getKey() {
        return key;
    }

    @Override
    public InputStream read() {
        try {
            return file.canRead() ? new BufferedInputStream(new FileInputStream(file)) : null;
        } catch (IOException ex) {
            throw new StoreException(ex);
        }
    }

    @Override
    public OutputStream write() {
        try {
            return new BufferedOutputStream(new FileOutputStream(file));
        } catch (IOException ex) {
            throw new StoreException(ex);
        }
    }

    @Override
    public long lastModified() {
        return file.lastModified();
    }
}
