package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.Serializable;
import java.util.Comparator;
import org.jg.util.Tolerance;
import org.jg.util.Transform;

/**
 * Geometry object.
 *
 * @author tofar_000
 */
public interface Geom extends Cloneable, Serializable {

    /**
     * Get the bounds for this geometry
     *
     * @return
     */
    Rect getBounds();

    /**
     * Create a version of this geometry transformed using the transform given
     *
     * @param transform transform
     * @return a transformed geometry
     * @throws NullPointerException if transform was null
     */
    Geom transform(Transform transform) throws NullPointerException;

    /**
     * Get a path iterator based on this geometry
     *
     * @return path iterator
     */
    PathIterator pathIterator();

    /**
     * Create a deep copy of this geom - since geoms are immutable, usually just
     * returns this
     *
     * @return
     */
    Geom clone();

    /**
     * Convert this geometry to a string. String is not WKT, as WKT may not be able to handle all geometry types
     *
     * @param appendable appendable
     * @throws NullPointerException if appendable was null
     * @throws GeomException if there was an IO error
     */
    void toString(Appendable appendable) throws NullPointerException, GeomException;

    /** 
     * Convert this to a standard format GeoShape
     * @param linearizer converter for arcs to lines if required
     * @param accuracy
     * @return 
     * @throws NullPointerException if accuracy or linearizer was null
     */
    GeoShape toGeoShape(Linearizer linearizer, Tolerance accuracy) throws NullPointerException;
    
    /**
     * Add this shape to the network given, using the linearizer given to convert
     * any curves to lines
     *
     * @param network network to which to add this geometry
     * @param linearizer number of lines used to approximate a quadrant
     * @param accuracy
     * @throws NullPointerException if network or accuracy was null
     */
    void addTo(Network network, Linearizer linearizer, Tolerance accuracy) throws NullPointerException;

    /**
     * Create a buffered version of this geometry
     *
     * @param amt amount by which to buffer - may be positive or negative
     * @param linearizer converter for arcs to lines if required
     * @param accuracy tolerance for inaccuracy
     * @return buffered geometry
     * @throws IllegalArgumentException if amt was infinite or NaN
     * @throws NullPointerException if tolerance or linearizer was null
     */
    Geom buffer(double amt, Linearizer linearizer, Tolerance accuracy) throws IllegalArgumentException, NullPointerException;

    /**
     * Get the relation between this geometry and the vector given
     *
     * @param vect vector
     * @param accuracy tolerance for inaccuracy
     * @return relation
     * @throws NullPointerException if vect or tolerance was null
     */
    int relate(Vect vect, Tolerance accuracy) throws NullPointerException;

    /**
     * Get the relation between this geometry and the vector given
     *
     * @param vect vector
     * @param accuracy tolerance for inaccuracy
     * @return relation
     * @throws NullPointerException if vect or tolerance was null
     */
    int relate(VectBuilder vect, Tolerance accuracy) throws NullPointerException;

    /**
     * Get the relation between this geometry and the geometry given
     *
     * @param geom geometry
     * @param linearizer converter for arcs to lines if required
     * @param accuracy tolerance for inaccuracy
     * @return relation
     * @throws NullPointerException if geom or accuracy was null
     */
    int relate(Geom geom, Linearizer linearizer, Tolerance accuracy) throws NullPointerException;
    
    /**
     * Get the area of this geometry.
     * 
     * @param linearizer converter for arcs to lines if required
     * @param accuracy
     * @return
     * @throws NullPointerException if accuracy was null
     */
    double getArea(Linearizer linearizer, Tolerance accuracy) throws NullPointerException;
    
    /**
     * Get the union of this geometry and that given. Any point touching one of
     * the geometries should be touching the result, and any point inside either
     * geometry should be inside the result
     *
     * @param other other geometry
     * @param linearizer converter for arcs to lines if required
     * @param accuracy tolerance for inaccuracy
     * @return union geometry
     * @throws NullPointerException if other or tolerance was null
     */
    Geom union(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException;

    /**
     * Get the intersection of this geometry and that given. Any point touching
     * both geometries should be touching the result, and any point inside
     * both geometries should be inside the result
     *
     * @param other other geometry
     * @param linearizer converter for arcs to lines if required
     * @param accuracy tolerance for inaccuracy
     * @return union geometry
     * @throws NullPointerException if other or tolerance was null
     */
    Geom intersection(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException;

    /**
     * Get the product of this geometry less the geometry given. Any point inside the
     * geometry given should be outside the result
     * @param other other geometry
     * @param linearizer converter for arcs to lines if required
     * @param accuracy tolerance for inaccuracy
     * @return this less other
     * @throws NullPointerException if other or tolerance was null
     */
    Geom less(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException;
    
    /**
     * Get the geometry which relresents everything touching or inside one geometry but not the
     * other.
     * @param other other geometry
     * @param linearizer converter for arcs to lines if required
     * @param accuracy tolerance for inaccuracy
     * @return this less other
     * @throws NullPointerException if other or tolerance was null
     */
    Geom xor(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException;
    
    /**
     * Comparator for comparing geometries by their Bounds
     */
    static final Comparator<Geom> COMPARATOR = new Comparator<Geom>(){
        
        @Override
        public int compare(Geom g1, Geom g2) {
            final double minX1, minY1, maxX1, maxY1, minX2, minY2, maxX2, maxY2;
            if(g1 instanceof Vect){
                Vect v1 = (Vect)g1;
                minX1 = maxX1 = v1.x;
                minY1 = maxY1 = v1.y;
            }else{
                Rect b1 = g1.getBounds();
                minX1 = b1.minX;
                minY1 = b1.minY;
                maxX1 = b1.maxX;
                maxY1 = b1.maxY;
            }
            if(g2 instanceof Vect){
                Vect v2 = (Vect)g2;
                minX2 = maxX2 = v2.x;
                minY2 = maxY2 = v2.y;
            }else{
                Rect b2 = g2.getBounds();
                minX2 = b2.minX;
                minY2 = b2.minY;
                maxX2 = b2.maxX;
                maxY2 = b2.maxY;
            }
            int c = Vect.compare(minX1, minY1, minX2, minY2);
            if(c == 0){
                c = Vect.compare(maxX1, maxY1, maxX2, maxY2);
            }
            return c;
        }
        
    };
}
