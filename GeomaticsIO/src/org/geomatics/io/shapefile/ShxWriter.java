package org.geomatics.io.shapefile;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author tofarrell
 */
public class ShxWriter implements AutoCloseable {

    private final OutputStream out;

    public ShxWriter(OutputStream out, ShpHeader header) throws IOException {
        if (out == null) {
            throw new NullPointerException("Out must not be null");
        }
        this.out = out;
        header.write(out);
    }

    void write(long offset, long contentLength) throws IOException {
        IO.writeUI32BE(offset, out);
        IO.writeUI32BE(contentLength, out);
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
