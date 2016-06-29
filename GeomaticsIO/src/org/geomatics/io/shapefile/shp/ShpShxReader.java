package org.geomatics.io.shapefile.shp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.geomatics.geom.Rect;
import org.om.store.json.RandomAccessFileInputStream;

/**
 *
 * @author tofarrell
 */
public class ShpShxReader implements ShpReader {

    private final ShxReader shxReader;
    private final RandomAccessFile shpFile;
    private final ShpOnlyReader shpReader;

    public ShpShxReader(String fileName) throws IOException {
        fileName = stripSuffix(fileName);
        shxReader = new ShxReader(new BufferedInputStream(new FileInputStream(fileName + ".shx")));
        try {
            shpFile = new RandomAccessFile(fileName + ".shp", "r");
            shpReader = new ShpOnlyReader(new RandomAccessFileInputStream(shpFile), shxReader.getHeader());
        } catch (IOException ex) {
            shxReader.close();
            throw ex;
        }
    }

    public ShpShxReader(File file) throws IOException {
        this(file.getAbsolutePath());
    }

    static String stripSuffix(String path) {
        int index = path.lastIndexOf('.');
        int index2 = path.lastIndexOf(File.separatorChar);
        if (index > index2) {
            path = path.substring(0, index);
        }
        return path;
    }

    @Override
    public ShpHeader getHeader() {
        return shpReader.getHeader();
    }

    @Override
    public long getRecordNumber() {
        return shpReader.getRecordNumber();
    }

    @Override
    public long getRecordLength() {
        return shpReader.getRecordLength();
    }

    @Override
    public ShapeType getShapeType() {
        return shpReader.getShapeType();
    }

    @Override
    public int[] getSegments() {
        return shpReader.getSegments();
    }

    @Override
    public double[] getOrds() {
        return shpReader.getOrds();
    }

    @Override
    public Rect getBounds() {
        return shpReader.getBounds();
    }

    @Override
    public boolean next() throws IOException {
        if (!shxReader.next()) {
            return false;
        }
        shpFile.seek(shxReader.getOffset() * 2); // offset is in words not bytes so multiply by 2
        if (!shpReader.next()) {
            throw new IOException("Error reading entry");
        }
        return true;
    }

    @Override
    public void close() throws IOException {
        try {
            shxReader.close();
        } finally {
            shpFile.close();
        }
    }

}
