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

    static final List<Ring> NO_RINGS = new ArrayList<>();
    static final List<LineString> NO_LINES = new ArrayList<>();
    static final VectList NO_POINTS = new VectList();
    public static final GeoShape EMPTY = new GeoShape(NO_RINGS, NO_LINES, NO_POINTS, true, null);
    // Area for this geom (may be null) 
    final List<Ring> rings;
    //ine strings which are not part of an area 
    final List<LineString> hangLines;
    // Points which are not part of a line 
    final VectList points;
    // Flag indicating whether or not this geom is normalized 
    Boolean normalized;
    // Bounds for this geo shape 
    Rect bounds;
    
    GeoShape(List<Ring> rings, List<LineString> hangLines, VectList points, Boolean normalized, Rect bounds) {
        this.rings = rings;
        this.hangLines = hangLines;
        this.points = points;
        this.normalized = normalized;
        this.bounds = bounds;
    }
    
    GeoShape(List<Ring> rings, List<LineString> hangLines, VectList points) {
        this.rings = rings;
        this.hangLines = hangLines;
        this.points = points;
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

    @Override
    public GeoShape intersection(Geom other, Tolerance flatness, Tolerance tolerance) throws NullPointerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GeoShape less(Geom other, Tolerance flatness, Tolerance tolerance) throws NullPointerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public GeoShape normalize(Tolerance accuracy){
        
    }
    
    public String toWKT(){
        
    }
}
