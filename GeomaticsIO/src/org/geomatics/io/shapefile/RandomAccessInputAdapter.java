package org.geomatics.io.shapefile;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 *
 * @author tofarrell
 */
class RandomAccessInputAdapter extends InputStream {

    private final RandomAccessFile file;

    RandomAccessInputAdapter(RandomAccessFile file) {
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
    public int available() throws IOException {
        long available = file.length() - file.getFilePointer();
        return available > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) available;
    }

    @Override
    public long skip(long n) throws IOException {
        long len = file.length();
        long offset = file.getFilePointer();
        long dst = Math.min(len, n + offset);
        file.seek(dst);
        return (dst - offset);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return file.read(b, off, len);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return file.read(b);
    }

    @Override
    public int read() throws IOException {
        return file.read();
    }

}
