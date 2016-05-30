package org.geomatics.io.shapefile;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author tofarrell
 */
public class DbfReader implements AutoCloseable {

    private final InputStream in;
    private final DbfHeader header;
    private final Object[] row;

    public DbfReader(InputStream in, DbfHeader header) {
        if(in == null){
            throw new NullPointerException("In must not be null");
        }
        this.in = in;
        this.header = header;
        this.row = new Object[header.getFields().length];
    }

    public DbfReader(InputStream in) throws IOException {
        this.in = in;
        this.header = DbfHeader.read(in);
        this.row = new Object[header.getFields().length];
    }

    public DbfHeader getHeader() {
        return header;
    }

    public Object[] getRow() {
        return row;
    }

    public boolean next() throws IOException {
        if(in.available() > 0){
            header.readData(in, row);
            return true;
        }
        return false;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
