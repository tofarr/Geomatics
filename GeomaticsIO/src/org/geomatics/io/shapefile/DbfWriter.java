package org.geomatics.io.shapefile;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author tofarrell
 */
public class DbfWriter implements AutoCloseable {

    private final OutputStream out;
    private final DbfHeader header;

    public DbfWriter(OutputStream out, DbfHeader header, boolean writeHeader) throws IOException {
        if (out == null) {
            throw new NullPointerException("Out must not be null");
        }
        this.out = out;
        this.header = header;
        if (writeHeader) {
            header.write(out);
        }
    }

    public DbfHeader getHeader() {
        return header;
    }

    public void write(Object[] row) throws IOException {
        header.writeData(row, out);
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
