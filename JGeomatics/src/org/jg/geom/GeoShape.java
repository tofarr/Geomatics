package org.jg.geom;

import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.VectList;

//TODO: Do not need multi point
//TODO: Rename to LineGeom
/**
 *
 * @author tofarrell
 */
public class GeoShape implements Geom {

    static final LineString[] NO_LINES = new LineString[0];
    public static final GeoShape EMPTY = new GeoShape(null, NO_LINES, null, true, null);

    static Geom reduce(Area area, MultiLineString lines, MultiPoint mp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    // Area for this geom (may be null) 
    final Area area;
    //ine strings which are not part of an area 
    final MultiLineString lines;
    // Points which are not part of a line 
    final MultiPoint points;
    // Flag indicating whether or not this geom is normalized 
    Boolean normalized;
    // Bounds for this geo shape 
    Rect bounds;

    GeoShape(Area area, MultiLineString lines, MultiPoint points, Boolean normalized, Rect bounds) {
        this(area, lines, points);
        this.normalized = normalized;
        this.bounds = bounds;
    }

    GeoShape(Area area, MultiLineString lines, MultiPoint points) {
        if(true){
            throw new UnsupportedOperationException("Rename to LineGeom");
        }
        this.area = area;
        this.lines = lines;
        this.points = points;
    }

    public static GeoShape valueOf(Network network, Tolerance accuracy) {
        network.clone();
        network.explicitIntersections(accuracy);
        return valueOfInternal(network, accuracy);
    }

    static GeoShape valueOfInternal(Network network, Tolerance accuracy) {
        if(true){
            throw new UnsupportedOperationException("Can return other types of geom?");
        }
        VectList points = new VectList();
        network.extractPoints(points);
        List<VectList> linePaths = new ArrayList<>();
        network.extractHangLines(linePaths);
        Area area = Area.valueOfInternal(network, accuracy);
        if (area != null) { // validate points and lines against area
            Network lineNetwork = new Network();
            for (VectList linePath : linePaths) {
                lineNetwork.addAllLinks(linePath);
            }
            lineNetwork.addAllVertices(points);
            area.removeWithRelation(lineNetwork, accuracy, Relate.INSIDE);
            points.clear();
            network.extractPoints(points);
            linePaths.clear();
            network.extractHangLines(linePaths);
        }

        LineString[] lines = linePaths.isEmpty() ? NO_LINES : new LineString[linePaths.size()];
        for (int i = lines.length; i-- > 0;) {
            LineString line = new LineString(linePaths.get(i));
            line.normalized = true;
            lines[i] = line;
        }
        if (points.isEmpty()) {
            points = null;
        }

        return new GeoShape(area, lines, points);
    }

    @Override
    public Rect getBounds() {
        Rect ret = bounds;
        if(ret == null){
            RectBuilder builder = new RectBuilder();
            if(area != null){
                builder.add(area.getBounds());
            }
            for(int i = lines.length; i-- > 0;){
                builder.add(lines[i].getBounds());
            }
            if(points != null){
                builder.add(points.getBounds());
            }
            ret = builder.build();
            bounds = ret;
        }
        return ret;
    }

    @Override
    public Geom transform(Transform transform) throws NullPointerException {
        Area _area = (area == null) ? null : area.transform(transform);
        LineString[] _lines = new LineString[lines.length];
        for(int i = _lines.length; i-- > 0;){
            _lines[i] = lines[i].transform(transform);
        }
        Arrays.sort(_lines, COMPARATOR);
        VectList _points = null;
        if(points != null){
            _points = points.clone();
            _points.transform(transform);
            _points.sort();
        }
        return new GeoShape(_area, _lines, _points);
    }

    @Override
    public PathIterator pathIterator() {
        return new PathIterator(){
            int state = -1;

            @Override
            public int getWindingRule() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public boolean isDone() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void next() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public int currentSegment(float[] coords) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public int currentSegment(double[] coords) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
            
        };
    }

    @Override
    public Geom clone() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void toString(Appendable appendable) throws NullPointerException, GeomException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GeoShape toGeoShape(Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        return this;
    }

    @Override
    public Geom buffer(double amt, Tolerance flatness, Tolerance tolerance) throws IllegalArgumentException, NullPointerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Relate relate(Vect vect, Tolerance tolerance) throws NullPointerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Relate relate(VectBuilder vect, Tolerance tolerance) throws NullPointerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GeoShape union(Geom other, Tolerance flatness, Tolerance tolerance) throws NullPointerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public GeoShape union(GeoShape other) {

    }

    @Override
    public GeoShape intersection(Geom other, Tolerance flatness, Tolerance tolerance) throws NullPointerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public GeoShape intersection(GeoShape other) {

    }

    @Override
    public GeoShape less(Geom other, Tolerance flatness, Tolerance tolerance) throws NullPointerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public GeoShape less(GeoShape other) {

    }

    public GeoShape normalize(Tolerance accuracy) {

    }

    public String toWKT() {

    }

//    void addNonRingsTo(Network network) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    void addNonRingsTo(Network network) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
    Area getArea() {

    }

    boolean hasArea() {

    }

    boolean hasPoints() {

    }

    boolean hasLines() {

    }

    boolean hasNonLines() {

    }

    boolean hasNonArea() {

    }

    boolean hasNonPoints() {

    }

    boolean isEmpty() {

    }

    void addTo(Network network) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void addLinesTo(Network network) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void removeWithRelation(Network network, Tolerance accuracy, Relate relate) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void addLinesTo(Network network) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void addLinesTo(Network network) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String toWKT() {
        final VectList points = new VectList();
        forEachVertex(new VertexProcessor() {
            @Override
            public boolean process(double x, double y, int numLinks) {
                if (numLinks == 0) {
                    points.add(x, y);
                }
                return true;
            }

        });
        StringBuilder str = new StringBuilder();
        if (points.size() == 0) {
            str.append("MULTILINESTRING(");
            List<VectList> lineStrings = new ArrayList<>();
            extractLines(lineStrings, false);
            for (int i = 0; i < lineStrings.size(); i++) {
                VectList lineString = lineStrings.get(i);
                if (i != 0) {
                    str.append(',');
                }
                str.append('(');
                for (int j = 0; j < lineString.size(); j++) {
                    if (j != 0) {
                        str.append(", ");
                    }
                    str.append(Vect.ordToStr(lineString.getX(j))).append(' ').append(Vect.ordToStr(lineString.getY(j)));
                }
                str.append(')');
            }
        } else if (points.size() == map.size()) {
            str.append("MULTIPOINT(");
            for (int i = 0; i < points.size(); i++) {
                if (i != 0) {
                    str.append(", ");
                }
                str.append(Vect.ordToStr(points.getX(i))).append(' ').append(Vect.ordToStr(points.getY(i)));
            }
        } else {
            str.append("GEOMETRYCOLLECTION(");
            for (int i = 0; i < points.size(); i++) {
                if (i != 0) {
                    str.append(",");
                }
                str.append("POINT(").append(Vect.ordToStr(points.getX(i))).append(' ').append(Vect.ordToStr(points.getY(i))).append(')');
            }
            List<VectList> lineStrings = new ArrayList<>();
            extractLines(lineStrings, false);
            for (int i = 0; i < lineStrings.size(); i++) {
                VectList lineString = lineStrings.get(i);
                str.append(",LINESTRING(");
                for (int j = 0; j < lineString.size(); j++) {
                    if (j != 0) {
                        str.append(", ");
                    }
                    str.append(Vect.ordToStr(lineString.getX(j))).append(' ').append(Vect.ordToStr(lineString.getY(j)));
                }
                str.append(')');
            }
        }
        str.append(')');
        return str.toString();
    }

    int numPoints() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void addPointsTo(VectList points) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
