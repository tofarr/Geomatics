package org.geomatics.io.shapefile.shp;

import org.geomatics.io.shapefile.IO;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author tofarrell
 */
public class ShxReader implements AutoCloseable {

    private final InputStream in;
    private final ShpHeader header;
    private long offset;
    private long contentLength;

    public ShxReader(InputStream in) throws IOException {
        this.in = in;
        header = ShpHeader.read(in);
    }

    public ShxReader(InputStream in, ShpHeader header) {
        if (in == null) {
            throw new NullPointerException("In must not be null");
        }
        this.in = in;
        this.header = header;
    }

    public ShpHeader getHeader() {
        return header;
    }

    public long getOffset() {
        return offset;
    }

    public long getContentLength() {
        return contentLength;
    }

    public boolean next() throws IOException {
        if (in.available() <= 0) {
            offset = contentLength = 0;
            return false;
        }
        offset = IO.readUI32BE(in);
        contentLength = IO.readUI32BE(in); // length in 16 bit words
        return true;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
