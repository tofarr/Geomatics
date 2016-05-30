package org.geomatics.io.shapefile;

import java.io.IOException;
import org.geomatics.geom.Rect;

/**
 *
 * @author tofarrell
 */
 public interface ShpReader extends AutoCloseable {

    ShpHeader getHeader();

     long getRecordNumber();

     long getRecordLength();

     ShapeType getShapeType();

     int[] getSegments();

     double[] getOrds();

     Rect getBounds();

     boolean next() throws IOException;

    @Override
     void close() throws IOException;
    
}
