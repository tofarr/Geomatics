package org.geomatics.io.shapefile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import org.geomatics.geom.Area;
import org.geomatics.geom.DefaultGeomFactory;
import org.geomatics.geom.Geom;
import org.geomatics.geom.GeomFactory;
import org.geomatics.geom.LineString;
import org.geomatics.geom.Network;
import org.geomatics.geom.Vect;
import org.geomatics.util.Tolerance;
import org.geomatics.util.VectList;
import org.geomatics.util.VectSet;

/**
 *
 * @author tofarrell
 */
public class GeomReader implements AutoCloseable {

    private final ShpReader reader;
    private final Tolerance accuracy;
    private final GeomFactory factory;
    private Geom geom;

    public GeomReader(ShpReader reader, Tolerance accuracy, GeomFactory factory) throws NullPointerException {
        if (reader == null) {
            throw new NullPointerException("Reader must not be null");
        }
        if (accuracy == null) {
            throw new NullPointerException("Accuracy must not be null");
        }
        if (factory == null) {
            throw new NullPointerException("Factory must not be null");
        }
        this.reader = reader;
        this.accuracy = accuracy;
        this.factory = factory;
    }

    public GeomReader(ShpReader reader) {
        this(reader, Tolerance.DEFAULT, new DefaultGeomFactory(Tolerance.DEFAULT));
    }

    public boolean next() throws IOException {
        if (!reader.next()) {
            geom = null;
            return false;
        }
        switch (reader.getShapeType()) {
            case POINT:
                createPoint();
                return true;
            case POLYLINE:
                createMultiLineString();
                return true;
            case POLYGON:
                createPolygon();
                return true;
            case MULTIPOINT:
                createMultiPoint();
                return true;
            case NULL:
                geom = null;
                return true;
            default:
                throw new UnsupportedOperationException("Unsupported type : " + reader.getShapeType());
        }
    }

    public long getRecordNumber() {
        return reader.getRecordNumber();
    }

    public Geom getGeom() {
        return geom;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    void createPoint() {
        double[] ords = reader.getOrds();
        geom = Vect.valueOf(ords[0], ords[1]);
    }

    void createMultiPoint() {
        double[] ords = reader.getOrds();
        switch(ords.length){
            case 0:
                geom = null;
                break;
            case 2:
                geom = factory.vect(ords[0], ords[1]);
                break;
            default:
                geom = factory.pointSet(new VectSet(ords));
        }
    }

    void createMultiLineString() {
        int[] segments = reader.getSegments();
        double[] ords = reader.getOrds();
        LineString[] lineStrings = new LineString[segments.length];
        int prev = 0;
        for (int s = 0; s < segments.length; s++) {
            int next = segments[s] << 1;
            int numVects = (next - prev) >> 1;
            lineStrings[s] = factory.lineString(new VectList().addAll(ords, prev, numVects));
            prev = next;
        }
        this.geom = factory.lineSet(Arrays.asList(lineStrings));
    }

    void createPolygon() {
        int[] segments = reader.getSegments();
        double[] ords = reader.getOrds();
        if(segments.length == 1){
            geom = factory.area(factory.ring(new VectList(ords)), Collections.EMPTY_LIST);
        }else{
            Network network = new Network();
            int prev = 0;
            for (int s = 0; s < segments.length; s++) {
                int next = segments[s] << 1;
                int numVects = (next - prev) >> 1;
                network.addAllLinks(new VectList().addAll(ords, prev, numVects));
                prev = next;
            }
            geom = Area.valueOf(accuracy, network);
        }
    }
}
