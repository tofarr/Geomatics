package org.geomatics.io.shapefile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.geomatics.geom.Rect;

/**
 *
 * @author tofarrell
 */
public class ShpShxWriter implements ShpWriter {

    private final ShxWriter shxWriter;
    private final ShpOnlyWriter shpWriter;
    private long offset;

    public ShpShxWriter(String fileName, ShpHeader header) throws IOException {
        fileName = ShpShxReader.stripSuffix(fileName);
        shxWriter = new ShxWriter(new BufferedOutputStream(new FileOutputStream(fileName + ".shx")), header);
        try{
            shpWriter = new ShpOnlyWriter(new BufferedOutputStream(new FileOutputStream(fileName + ".shp")), header);
        }catch(IOException ex){
            shxWriter.close();
            throw ex;
        }
        offset = 50;
    }

    public ShpShxWriter(File file, ShpHeader header) throws IOException {
        this(file.getAbsolutePath(), header);
    }
    @Override
    public long write(long recordNumber, ShapeType shapeType, int[] segments, double[] ords, Rect bounds) throws IOException {
        long contentLength = shpWriter.write(recordNumber, shapeType, segments, ords, bounds);
        shxWriter.write(offset, contentLength);
        offset += contentLength;
        return contentLength;
    }

    @Override
    public void close() throws IOException {
        try{
            shxWriter.close();
        }finally{
            shpWriter.close();
        }
    }

}
