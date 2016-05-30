package org.geomatics.io.shapefile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.geomatics.geom.Geom;

/**
 *
 * @author tofarrell
 */
public class ShapeFileWriter implements AutoCloseable {

    private final GeomWriter geomWriter;
    private final DbfWriter dbfWriter;
    private long recordNumber;

    public ShapeFileWriter(File file, ShpHeader shpHeader, DbfHeader dbfHeader) throws IOException {
        String path = file.getAbsolutePath();
        int index = path.lastIndexOf('.');
        int index2 = path.lastIndexOf(File.separatorChar);
        if (index > index2) {
            path = path.substring(0, index);
        }
        this.geomWriter = new GeomWriter(new ShpOnlyWriter(new BufferedOutputStream(new FileOutputStream(path + ".shp")), shpHeader));
        try {
            this.dbfWriter = new DbfWriter(new BufferedOutputStream(new FileOutputStream(path + ".dbf")), dbfHeader, true);
        } catch (IOException ex) {
            this.geomWriter.close();
            throw ex;
        }
        recordNumber = 1;
    }

    public void write(Object[] row, Geom geom) throws IOException{
        geomWriter.write(recordNumber++, geom);
        dbfWriter.write(row);
    }
    
    public DbfField[] getFields() {
        return dbfWriter.getHeader().getFields();
    }

    @Override
    public void close() throws IOException {
        try {
            this.geomWriter.close();
        } finally {
            this.dbfWriter.close();
        }
    }

}
