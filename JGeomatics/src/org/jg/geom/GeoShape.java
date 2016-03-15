package org.jg.geom;

import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Collections;
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

    static final List<LineString> NO_LINES = new ArrayList<>();
    static final VectList NO_POINTS = new VectList();
    public static final GeoShape EMPTY = new GeoShape(Area.EMPTY, NO_LINES, NO_POINTS, true, null);
    // Area for this geom (may be null) 
    final Area area;
    //ine strings which are not part of an area 
    final List<LineString> lines;
    // Points which are not part of a line 
    final VectList points;
    // Flag indicating whether or not this geom is normalized 
    Boolean normalized;
    // Bounds for this geo shape 
    Rect bounds;

    GeoShape(Area area, List<LineString> hangLines, VectList points, Boolean normalized, Rect bounds) {
        this.area = area;
        this.hangLines = hangLines;
        this.points = points;
        this.normalized = normalized;
        this.bounds = bounds;
    }

    GeoShape(Area area, List<LineString> hangLines, VectList points) {
        this.area = area;
        this.hangLines = hangLines;
        this.points = points;
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

    
    
    public static Area valueOf(Network network) {
        List<Ring> rings = Ring.valueOf(network);
        switch (rings.size()) {
            case 0:
                return null;
            case 1:
                return new Area(rings.get(0), Area.EMPTY);
            default:
                RingSetBuilder builder = new RingSetBuilder(null);
                for (Ring ring : rings) {
                    builder.add(ring);
                }
                return builder.build();
        }
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

    void addNonRingsTo(Network network) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void addNonRingsTo(Network network) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

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
    
    
    static class RingSetBuilder {

        final Ring shell;
        final ArrayList<RingSetBuilder> children;

        RingSetBuilder(Ring shell) {
            this.shell = shell;
            children = new ArrayList<>();
        }

        Area build() {
            if (shell == null) {
                if (children.size() == 1) {
                    return children.get(0).build();
                }
            }
            Area[] _children = new Area[children.size()];
            for (int c = 0; c < _children.length; c++) {
                _children[c] = children.get(c).build();
            }
            Area ret = new Area(shell, _children);
            ret.valid = true;
            return ret;
        }

        boolean add(Ring ring) {
            if (!canAdd(ring)) {
                return false;
            }
            for (RingSetBuilder child : children) {
                if (child.add(ring)) {
                    return true;
                }
            }
            children.add(new RingSetBuilder(ring));
            return true;
        }

        boolean canAdd(Ring ring) {
            if (shell == null) {
                return true;
            }
            int i = 0;
            while (true) {
                Relate relate = shell.relate(ring.vects.getX(i), ring.vects.getY(i), Tolerance.ZERO);
                switch (relate) {
                    case INSIDE:
                        return true;
                    case OUTSIDE:
                        return false;
                }
                i++;
            }
        }
    }
}
