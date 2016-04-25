package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.IOException;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.VectList;
import org.jg.util.VectSet;

/**
 * Immutable set of 2D points. Checks are in place to insure that no ordinates are NaN or Infinite, and that no point is repeated. Points
 * are ordered in the standard Natural ordering for vectors
 *
 * @author tofar
 */
public final class PointSet implements Geom {

    final VectList vects;

    PointSet(VectList vects) {
        this.vects = vects;
    }

    /**
     * Get / Create a PointSet based on the set of vectors given
     *
     * @param vectSet
     * @return a PointSet, or null if the set of vectors was empty
     * @throws NullPointerException if vectSet was null
     */
    public static PointSet valueOf(VectSet vectSet) throws NullPointerException {
        if (vectSet.isEmpty()) {
            return null;
        }
        VectList vects = new VectList();
        vectSet.toList(vects);
        vects.sort();
        return new PointSet(vects);
    }
    
    /**
     * Get a pointset based on the oridnates given
     * 
     * @param ords
     * @return 
     */
    public static PointSet valueOf(double ... ords) throws NullPointerException, IllegalArgumentException{
        VectSet vects = new VectSet(ords);
        return PointSet.valueOf(vects);
    }

    /**
     * Extract points from the network given
     * @param network
     * @param accuracy
     * @return
     * @throws NullPointerException
     */
    public static PointSet valueOf(Network network, Tolerance accuracy) throws NullPointerException {
        network = network.clone();
        network.explicitIntersections(accuracy);
        network.snap(accuracy);
        return valueOfInternal(network);
    }
    
    static PointSet valueOfInternal(Network network) throws NullPointerException {
        VectList vects = new VectList();
        network.extractPoints(vects);
        vects.sort();
        return vects.isEmpty() ? null : new PointSet(vects);
    }

    @Override
    public Rect getBounds() {
        return vects.getBounds();
    }

    @Override
    public PointSet transform(Transform transform) throws NullPointerException {
        if (transform.mode == Transform.NO_OP) {
            return this;
        }
        VectList _vects = vects.clone();
        _vects.transform(transform);
        _vects.sort();
        return new PointSet(_vects);
    }

    public Geom simplify() {
        return (vects.size() == 1) ? vects.getVect(0) : this;
    }

    @Override
    public PathIterator pathIterator() {
        return new PathIterator() {

            final int max = vects.size() - 1;
            int index;
            int seg = SEG_MOVETO;

            @Override
            public int getWindingRule() {
                return WIND_NON_ZERO;
            }

            @Override
            public boolean isDone() {
                return (index > max);
            }

            @Override
            public void next() {
                if (seg == SEG_MOVETO) {
                    seg = SEG_LINETO;
                } else if (index <= max) {
                    index++;
                    seg = SEG_MOVETO;
                }
            }

            @Override
            public int currentSegment(float[] coords) {
                coords[0] = (float) vects.getX(index);
                coords[1] = (float) vects.getY(index);
                return seg;
            }

            @Override
            public int currentSegment(double[] coords) {
                coords[0] = vects.getX(index);
                coords[1] = vects.getY(index);
                return seg;
            }

        };
    }

    @Override
    public PointSet clone() {
        return this;
    }

    @Override
    public void toString(Appendable appendable) throws NullPointerException, GeomException {
        try {
            appendable.append("[\"PS\"");
            for (int i = 0; i < vects.size(); i++) {
                appendable.append(", ").append(Vect.ordToStr(vects.getX(i))).append(',').append(Vect.ordToStr(vects.getY(i)));
            }
            appendable.append(']');
        } catch (IOException ex) {
            throw new GeomException("Error writing LineStirng", ex);
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        toString(str);
        return str.toString();
    }

    @Override
    public GeoShape toGeoShape(Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        return toGeoShape();
    }

    /**
     * Convert this PointSet to a GeoShape
     * @return
     */
    public GeoShape toGeoShape() {
        return new GeoShape(null, null, this);
    }

    @Override
    public void addTo(Network network, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        addTo(network);
    }
    
    /**
     * Add this PointSet to a Network
     * @param network
     */
    public void addTo(Network network) {
        network.addAllVertices(vects);
    }

    @Override
    public Geom buffer(double amt, Linearizer linearizer, Tolerance accuracy) throws IllegalArgumentException, NullPointerException {
        if (amt < 0) {
            return null;
        } else if (amt == 0) {
            return this;
        }

        //Create a buffer at 0
        VectList point = new VectList();
        double angleSize = 2 * Math.PI;
        double sx = amt;
        point.add(sx, 0);
        linearizer.linearizeSegment(0, 0, sx, 0, angleSize, point);

        Area result = null;
        int s = point.size();
        for (int v = vects.size(); v-- > 0;) {
            VectList ringVects = new VectList();
            double x = vects.getX(v);
            double y = vects.getY(v);
            for(int i = 0; i < s; i++){
                double ax = point.getX(i) + x;
                double ay = point.getY(i) + y;
                ringVects.add(ax, ay);
            }
            Ring ring = new Ring(ringVects, null);
            result = (result == null) ? ring.toArea() : result.union(ring, accuracy);
        }
        return result;
    }

    @Override
    public int relate(Vect vect, Tolerance tolerance) throws NullPointerException {
        return relateInternal(vect.x, vect.y, tolerance);
    }

    @Override
    public int relate(VectBuilder vect, Tolerance tolerance) throws NullPointerException {
        return relateInternal(vect.getX(), vect.getY(), tolerance);
    }

    int relateInternal(double x, double y, Tolerance tolerance) throws NullPointerException {
        if (vects.getBounds().relateInternal(x, y, tolerance) == Relation.DISJOINT) {
            return Relation.DISJOINT;
        }
        for (int v = vects.size(); v-- > 0;) {
            if (tolerance.match(vects.getX(v), vects.getY(v), x, y)) {
                int ret = Relation.TOUCH;
                if(vects.size() > 1){
                    ret |= Relation.A_OUTSIDE_B;
                }
                return ret;
            }
        }
        return Relation.DISJOINT;
    }
    
    public int relate(PointSet other, Tolerance accuracy){
        if(getBounds().relate(other.getBounds(), accuracy) == Relation.DISJOINT){
            return Relation.DISJOINT; // if bounds do not overlap, then content cannot possibly overlap
        }
        int ret = Relation.NULL;
        int i = numPoints()-1;
        int j = other.numPoints()-1;
        while((i >= 0) && (j >= 0) && (ret != (Relation.TOUCH | Relation.A_OUTSIDE_B | Relation.B_OUTSIDE_A))){
            double ix = getX(i);
            double iy = getY(i);
            double jx = other.getX(j);
            double jy = other.getY(j);
            if(accuracy.match(ix, iy, jx, jy)){
                i--;
                j--;
                ret |= Relation.TOUCH;
            }else if(Vect.compare(ix, iy, jx, jy) < 0){
                j--;
                ret |= Relation.B_OUTSIDE_A;
            }else{
                i--;
                ret |= Relation.A_OUTSIDE_B;
            }
        }
        if(i > 0){
            ret |= Relation.A_OUTSIDE_B;
        }else if(j > 0){
            ret |= Relation.B_OUTSIDE_A;
        }
        return ret;
    }

    @Override
    public int relate(Geom geom, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        if(geom instanceof Vect){
            return relate((Vect)geom, accuracy);
        }else if(geom instanceof PointSet){
            return relate((PointSet)geom, accuracy);
        }else{
            int ret = NetworkRelationProcessor.relate(this, geom, linearizer, accuracy);
            return ret;
        }
    }
    
    @Override
    public Geom union(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        if (other instanceof Vect) {
            return union((Vect)other, accuracy).simplify();
        } else if (other instanceof PointSet) {
            return union((PointSet) other, accuracy).simplify();
        } else {
            return toGeoShape().union(other, linearizer, accuracy);
        }
    }
    
    public PointSet union(Vect vect, Tolerance accuracy) throws NullPointerException{
        if(Relation.isTouch(relate(vect, Tolerance.DEFAULT))){
            return this;
        }
        VectList points = new VectList(numPoints() + 1);
        points.addAll(vects);
        points.add(vect);
        points.sort();
        return new PointSet(points);
    }

    /**
     * Get the union of this point set and the point set given
     * @param other
     * @param accuracy
     * @return a point set
     * @throws NullPointerException if other or accuracy was null
     */
    public PointSet union(PointSet other, Tolerance accuracy) throws NullPointerException {
        VectList otherVects = other.vects;
        VectList newVects = new VectList(vects.size() + otherVects.size());
        newVects.addAll(vects);
        if(getBounds().relate(other.getBounds(), accuracy) == Relation.DISJOINT){
            newVects.addAll(other.vects);
        }else{
            VectBuilder vect = new VectBuilder();
            for (int i = 0; i < otherVects.size(); i++) {
                otherVects.getVect(i, vect);
                if (relate(vect, accuracy) == Relation.DISJOINT) {
                    newVects.add(vect);
                }
            }
            if (newVects.size() == vects.size()) {
                return this;
            } else if (newVects.size() == other.vects.size()) {
                return other;
            }
        }
        newVects.sort();
        return new PointSet(newVects);
    }

    @Override
    public Geom intersection(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        PointSet ret = intersection(other, accuracy);
        if ((ret != null) && (ret.numPoints() == 1)) {
            return ret.getPoint(0);
        }
        return ret;
    }

    /**
     * Get the intersection of this point set and the geom given
     * @param other
     * @param accuracy
     * @return a point set
     * @throws NullPointerException if other or accuracy was null
     */
    public PointSet intersection(Geom other, Tolerance accuracy) throws NullPointerException {
        if(getBounds().relate(other.getBounds(), accuracy) == Relation.DISJOINT){
            return null;
        }
        VectList ret = new VectList(vects.size());
        VectBuilder vect = new VectBuilder();
        for (int i = 0; i < vects.size(); i++) {
            vects.getVect(i, vect);
            if (other.relate(vect, accuracy) != Relation.DISJOINT) {
                ret.add(vect);
            }
        }
        if (ret.isEmpty()) {
            return null;
        } else if (ret.size() == vects.size()) {
            return this;
        } else {
            return new PointSet(ret);
        }
    }

    @Override
    public Geom less(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        PointSet ret = less(other, accuracy);
        return (ret == null) ? null : ret.simplify();
    }

    public PointSet less(Geom other, Tolerance accuracy) throws NullPointerException {
        if(getBounds().relate(other.getBounds(), accuracy) == Relation.DISJOINT){
            return this;
        }
        VectList ret = new VectList(vects.size());
        VectBuilder vect = new VectBuilder();
        for (int i = 0; i < vects.size(); i++) {
            vects.getVect(i, vect);
            if (other.relate(vect, accuracy) == Relation.DISJOINT) {
                ret.add(vect);
            }
        }
        if (ret.isEmpty()) {
            return null;
        } else if (ret.size() == vects.size()) {
            return this;
        } else {
            return new PointSet(ret);
        }
    }
      
    @Override
    public double getArea(Linearizer linearizer, Tolerance accuracy){
        return 0;
    }

    /**
     *
     * @return
     */
    public int numPoints() {
        return vects.size();
    }

    /**
     * Get point at index
     * @param index
     * @return
     * @throws IndexOutOfBoundsException
     */
    public Vect getPoint(int index) throws IndexOutOfBoundsException {
        return vects.getVect(index);
    }

    /**
     * Get point at index, and place it in target
     * @param index
     * @param target
     * @return target
     * @throws IndexOutOfBoundsException
     * @throws NullPointerException if target was null
     */
    public VectBuilder getPoint(int index, VectBuilder target) throws IndexOutOfBoundsException, NullPointerException {
        return vects.getVect(index, target);
    }

    /**
     * Get x value of point at index given
     * @param index
     * @return
     * @throws IndexOutOfBoundsException
     */
    public double getX(int index) throws IndexOutOfBoundsException {
        return vects.getX(index);
    }

    /**
     * Get y value of point at index given
     * @param index
     * @return
     * @throws IndexOutOfBoundsException
     */
    public double getY(int index) throws IndexOutOfBoundsException {
        return vects.getY(index);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + this.vects.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PointSet) {
            final PointSet other = (PointSet) obj;
            return this.vects.equals(other.vects);
        }
        return false;
    }
}
