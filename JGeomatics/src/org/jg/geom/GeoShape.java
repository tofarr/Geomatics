package org.jg.geom;

import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.VectList;

//TODO: Do not need multi point
/**
 *
 * @author tofarrell
 */
public class GeoShape implements Geom {

    static final LineString[] NO_LINES = new LineString[0];
    public static final GeoShape EMPTY = new GeoShape(null, NO_LINES, null, true, null);
    // Area for this geom (may be null) 
    final Area area;
    //ine strings which are not part of an area 
    final LineString[] lines;
    // Points which are not part of a line 
    final VectList points;
    // Flag indicating whether or not this geom is normalized 
    Boolean normalized;
    // Bounds for this geo shape 
    Rect bounds;

    GeoShape(Area area, LineString[] lines, VectList points, Boolean normalized, Rect bounds) {
        this.area = area;
        this.lines = lines;
        this.points = points;
        this.normalized = normalized;
        this.bounds = bounds;
    }

    GeoShape(Area area, LineString[] lines, VectList points) {
        this.area = area;
        this.lines = lines;
        this.points = points;
    }
    
    
    public Geom toGeom(){
        final List<Geom> geoms = new ArrayList<>();
        Area ringSet = Area.valueOf(this);
        int numVectsInRings;
        if(ringSet == null){
            numVectsInRings = 0;
        }else{
            numVectsInRings = ringSet.numVects();
            geoms.add(ringSet);
        }
        if(numVectsInRings < map.size()){
            final VectList points = new VectList();
            map.forEach(new VectMapProcessor<VectList>(){
                final VectList lineString = new VectList();
                @Override
                public boolean process(double x, double y, VectList value) {
                    switch(value.size()){
                        case 0:
                            points.add(x, y);
                            break;
                        case 1:
                            lineString.clear();
                            followLine(x, y, value.getX(0), value.getY(0), lineString);
                            int end = lineString.size()-1;
                            double endX = lineString.getX(end);
                            double endY = lineString.getY(end);
                            if((map.get(endX, endY).size() != 1) || Vect.compare(x, y, endX, endY) < 0){
                                if(!lineString.isOrdered()){
                                    lineString.reverse();
                                }
                                if(lineString.size() == 2){
                                    geoms.add(new Line(lineString.getX(0), lineString.getY(0), lineString.getX(1), lineString.getY(1)));
                                }else{
                                    geoms.add(new LineString(lineString.clone()));
                                }
                            }
                    }
                    return true;
                }
            
            });
            if(!points.isEmpty()){
                if(points.size() == 1){
                    geoms.add(points.getVect(0));
                }else{
                    geoms.add(new MultiPoint(points));
                }
            }
        }
        switch(geoms.size()){
            case 0:
                return null;
            case 1:
                return geoms.get(0);
            default:
                Geom[] ret = geoms.toArray(new Geom[geoms.size()]);
                Arrays.sort(ret, Geom.COMPARATOR);
                return new GeomSet(ret);
        }
    }

    
    
    public static GeoShape consumeNetwork(Network network, Tolerance accuracy){
        //network.explicitIntersections(accuracy);
        //while network is not empty...
            //get the min point in the network.
            //get line with lowest dydx.
            //follow line, getting next ccw link at each point
            //other linesets should be removed.
            //when get back to start, you have a linear ring
        
    }

    @Override
    public Rect getBounds() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Geom transform(Transform transform) throws NullPointerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PathIterator pathIterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
    
    boolean hasArea(){
        
    }
    
    boolean hasPoints(){
        
    }
    
    boolean hasLines(){
        
    }
    
    boolean hasNonLines(){
        
    }
    
    boolean hasNonArea(){
        
    }
    
    boolean hasNonPoints(){
        
    }
    
    boolean isEmpty(){
        
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
}
