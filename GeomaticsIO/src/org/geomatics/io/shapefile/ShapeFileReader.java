package org.geomatics.io.shapefile;

import org.geomatics.io.shapefile.GeomReader;
import org.geomatics.io.shapefile.shp.ShpOnlyReader;
import org.geomatics.io.shapefile.dbf.DbfReader;
import org.geomatics.io.shapefile.dbf.DbfField;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.geomatics.geom.Geom;

/**
 *
 * @author tofarrell
 */
public class ShapeFileReader implements AutoCloseable {

    private final GeomReader geomReader;
    private final DbfReader dbfReader;

    public ShapeFileReader(GeomReader geomReader, DbfReader dbfReader) {
        if (geomReader == null) {
            throw new NullPointerException("geomReader must not be null");
        }
        if (dbfReader == null) {
            throw new NullPointerException("dbfReader must not be null");
        }
        this.geomReader = geomReader;
        this.dbfReader = dbfReader;
    }

    public ShapeFileReader(File file) throws IOException {
        String path = file.getAbsolutePath();
        int index = path.lastIndexOf('.');
        int index2 = path.lastIndexOf(File.separatorChar);
        if (index > index2) {
            path = path.substring(0, index);
        }
        this.geomReader = new GeomReader(new ShpOnlyReader(new BufferedInputStream(new FileInputStream(path + ".shp"))));
        try {
            this.dbfReader = new DbfReader(new BufferedInputStream(new FileInputStream(path + ".dbf")));
        } catch (IOException ex) {
            this.geomReader.close();
            throw ex;
        }
    }
    
    public ShapeFileReader(String fileName) throws IOException{
        this(new File(fileName));
    }

    public boolean next() throws IOException {
        if (geomReader.next()) {
            if (!dbfReader.next()) {
                throw new IOException("shp contained more entries than dbf!");
            }
            return true;
        } else if (dbfReader.next()) {
            throw new IOException("dbf contained more entries than shp!");
        } else {
            return false;
        }
    }

    public DbfField[] getFields() {
        return dbfReader.getHeader().getFields();
    }

    public Geom getGeom() {
        return geomReader.getGeom();
    }

    public Object[] getRow() {
        return dbfReader.getRow();
    }

    @Override
    public void close() throws IOException {
        try {
            this.geomReader.close();
        } finally {
            this.dbfReader.close();
        }
    }

}
