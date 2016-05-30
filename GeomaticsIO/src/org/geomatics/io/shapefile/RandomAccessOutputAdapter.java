package org.geomatics.io.shapefile;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 *
 * @author tofarrell
 */
class RandomAccessOutputAdapter extends OutputStream {

    private final RandomAccessFile file;

    RandomAccessOutputAdapter(RandomAccessFile file) {
        if (file == null) {
            throw new NullPointerException("File must not be null");
        }
        this.file = file;
    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        file.write(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        file.write(b);
    }

    @Override
    public void write(int b) throws IOException {
        file.write(b);
    }

}
