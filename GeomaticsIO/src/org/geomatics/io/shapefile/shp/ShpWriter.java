package org.geomatics.io.shapefile.shp;

import java.io.IOException;
import org.geomatics.geom.Rect;

/**
 *
 * @author tofarrell
 */
public interface ShpWriter extends AutoCloseable {

    long write(long recordNumber, ShapeType shapeType, int[] segments, double[] ords, Rect bounds) throws IOException;
    
    void close() throws IOException;
}
