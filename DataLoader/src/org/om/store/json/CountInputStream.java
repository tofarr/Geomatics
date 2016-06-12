package org.om.store.json;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author tofar
 */
public class CountInputStream extends FilterInputStream {

    private long count;

    public CountInputStream(InputStream in) {
        super(in);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int ret = super.read(b, off, len);
        if (ret > 0) {
            count += ret;
        }
        return ret;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int ret = super.read(b);
        if (ret > 0) {
            count += ret;
        }
        return ret;
    }

    @Override
    public int read() throws IOException {
        int ret = super.read();
        if (ret > 0) {
            count++;
        }
        return ret;
    }

    @Override
    public long skip(long n) throws IOException {
        long ret = super.skip(n);
        count += ret;
        return ret;
    }

    public long getCount() {
        return count;
    }
}
