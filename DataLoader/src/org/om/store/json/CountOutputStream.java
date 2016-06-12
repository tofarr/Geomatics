package org.om.store.json;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author tofar
 */
public class CountOutputStream extends FilterOutputStream {

    private long count;

    public CountOutputStream(OutputStream out) {
        super(out);
    }

    public CountOutputStream(OutputStream out, long count) {
        super(out);
        this.count = count;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
        count += len;
    }

    @Override
    public void write(byte[] b) throws IOException {
        super.write(b);
        count += b.length;
    }

    @Override
    public void write(int b) throws IOException {
        super.write(b);
        count++;
    }

    public long getCount() {
        return count;
    }

}
