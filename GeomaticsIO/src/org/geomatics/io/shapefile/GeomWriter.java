package org.geomatics.io.shapefile;

import org.geomatics.io.shapefile.shp.ShpWriter;
import java.io.IOException;
import java.util.List;
import org.geomatics.geom.Area;
import org.geomatics.geom.GeoShape;
import org.geomatics.geom.Geom;
import org.geomatics.geom.LineSet;
import org.geomatics.geom.LineString;
import org.geomatics.geom.Linearizer;
import org.geomatics.geom.PointSet;
import org.geomatics.geom.Ring;
import org.geomatics.io.shapefile.shp.ShapeType;
import org.geomatics.util.Tolerance;

/**
 *
 * @author tofarrell
 */
public class GeomWriter implements AutoCloseable {

    private final ShpWriter writer;
    private final Linearizer linearizer;
    private final Tolerance accuracy;
    
    public GeomWriter(ShpWriter writer, Linearizer linearizer, Tolerance accuracy) {
        if (writer == null) {
            throw new NullPointerException("Writer must not be null");
        }
        this.writer = writer;
        this.linearizer = linearizer;
        this.accuracy = accuracy;
    }

    public void write(long recordNumber, Geom geom) throws IOException {
        if (geom == null) {
            writer.write(recordNumber, ShapeType.NULL, null, null, null);
            return;
        }
        GeoShape geoShape = geom.toGeoShape(linearizer, accuracy);
        if((geoShape.area == null) && (geoShape.lines == null)){
            PointSet points = geoShape.points;
            double[] ords = new double[points.numPoints()<<1];
            for(int i = 0, j = 0; j < ords.length; i++){
                ords[j++] = points.getX(i);
                ords[j++] = points.getY(i);
            }
            ShapeType type = (ords.length == 2) ? ShapeType.POINT : ShapeType.MULTIPOINT;
            writer.write(recordNumber, type, null, ords, geom.getBounds());
        }else if((geoShape.area == null) && (geoShape.points == null)){
            LineSet lines = geoShape.lines;
            int[] segments  = new int[lines.numLineStrings()];
            int offset = 0;
            for(int i = 0; i < segments.length; i++){
                segments[i] = offset;
                offset += lines.getLineString(i).numPoints();
            }
            double[] ords = new double[offset];
            offset = 0;
            for(int i = 0; i < segments.length; i++){
                LineString ls = lines.getLineString(i);
                for(int j = 0; j < ls.numPoints(); j++){
                    ords[offset++] = ls.getX(j);
                    ords[offset++] = ls.getY(j);
                }
            }
            writer.write(recordNumber, ShapeType.POLYLINE, null, ords, geom.getBounds());
        }else if((geoShape.lines == null) && (geoShape.points == null)){
            Area area = geoShape.area;
            area.numRings();
            List<Ring> rings = area.getRings();
            double[] ords = new double[area.numVects()];
            int offset = 0;
            for(Ring ring : rings){
                for(int j = 0; j < ring.numPoints(); j++){
                    ords[offset++] = ring.getX(j);
                    ords[offset++] = ring.getY(j);
                }
            }
            writer.write(recordNumber, ShapeType.POLYGON, null, ords, geom.getBounds());
        }else{
            throw new UnsupportedOperationException("Unsupported type " + geom.getClass());    
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
