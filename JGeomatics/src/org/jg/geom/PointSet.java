package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.IOException;
import org.jg.util.SpatialNode;
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

    static PointSet valueOf(Network network, Tolerance tolerance) throws NullPointerException {
        VectList vects = new VectList();
        network.extractPoints(vects);
        SpatialNode<Line> links = network.getLinks();
        for (int v = vects.size(); v-- > 0;) {
            if (network.pointTouchesLineInternal(vects.getX(v), vects.getY(v), links, tolerance)) {
                vects.remove(v);
            }
        }
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
    public GeoShape toGeoShape(Tolerance flatness, Tolerance accuracy) throws NullPointerException {
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
    public void addTo(Network network, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
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
    public Geom buffer(double amt, Tolerance flatness, Tolerance accuracy) throws IllegalArgumentException, NullPointerException {
        if (amt < 0) {
            return null;
        } else if (amt == 0) {
            return this;
        }

        //Create a buffer at 0
        VectList point = new VectList();
        double angleSize = 2 * Math.PI;
        double sy = amt;
        Vect.linearizeArcInternal(0, 0, angleSize, 0, sy, 0, sy, amt, flatness.getTolerance(), point);

        Network network = new Network();
        int s = point.size() - 1;
        for (int v = vects.size(); v-- > 0;) {
            double x = vects.getX(v);
            double y = vects.getY(v);
            double bx = point.getX(s) + x;
            double by = point.getY(s) + y;
            for (int i = s; i-- > 0;) {
                double ax = point.getX(i) + x;
                double ay = point.getY(i) + y;
                network.addLinkInternal(ax, ay, bx, by);
                bx = ax;
                by = ay;
            }
        }

        //remove any line which is too close to a point
        network.explicitIntersections(accuracy);
        removeWithinBuffer(vects, network, amt, flatness, accuracy);

        return Area.valueOfInternal(network, accuracy);
    }

    //remove any link from network with a mid point closer than the amt to one of the lines in this
    static void removeWithinBuffer(VectList vects, Network network, double amt, Tolerance flatness, Tolerance tolerance) {
        final SpatialNode<Line> lines = network.getLinks();
        double threshold = amt - flatness.tolerance;
        final NearLinkRemover remover = new NearLinkRemover(threshold, network);
        RectBuilder bounds = new RectBuilder();
        for (int i = vects.size(); i-- > 0;) {
            double x = vects.getX(i);
            double y = vects.getY(i);
            bounds.reset().add(x, y).buffer(threshold);
            remover.reset(x, y);
            lines.forOverlapping(bounds.build(), remover);
        }
    }

    @Override
    public Relate relate(Vect vect, Tolerance tolerance) throws NullPointerException {
        return relateInternal(vect.x, vect.y, tolerance);
    }

    @Override
    public Relate relate(VectBuilder vect, Tolerance tolerance) throws NullPointerException {
        return relateInternal(vect.getX(), vect.getY(), tolerance);
    }

    Relate relateInternal(double x, double y, Tolerance tolerance) throws NullPointerException {
        if (vects.getBounds().relateInternal(x, y, tolerance) == Relate.OUTSIDE) {
            return Relate.OUTSIDE;
        }
        for (int v = vects.size(); v-- > 0;) {
            if (tolerance.match(vects.getX(v), vects.getY(v), x, y)) {
                return Relate.TOUCH;
            }
        }
        return Relate.OUTSIDE;
    }

    @Override
    public Geom union(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        if (other instanceof Vect) {
            return ((Vect) other).union(this, accuracy);
        } else if (other instanceof PointSet) {
            return union((PointSet) other, accuracy);
        } else {
            return union(other.toGeoShape(flatness, accuracy), accuracy).simplify();
        }
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
        if(getBounds().isDisjoint(other.getBounds(), accuracy)){
            newVects.addAll(other.vects);
        }else{
            VectBuilder vect = new VectBuilder();
            for (int i = 0; i < otherVects.size(); i++) {
                otherVects.getVect(i, vect);
                if (relate(vect, accuracy) == Relate.OUTSIDE) {
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
    
    /**
     * Get the union of this point set and the geoshape given
     * @param other
     * @param accuracy
     * @return a point set
     * @throws NullPointerException if other or accuracy was null
     */
    public GeoShape union(GeoShape other, Tolerance accuracy) {
        VectList newVects = new VectList();
        if(other.points != null){
            newVects.addAll(other.points.vects);
        }
        VectBuilder vect = new VectBuilder();
        for (int i = 0; i < vects.size(); i++) {
            vects.getVect(i, vect);
            if (other.relate(vect, accuracy) == Relate.OUTSIDE) {
                newVects.add(vect);
            }
        }
        PointSet _points = newVects.isEmpty() ? null : new PointSet(newVects);
        GeoShape ret = new GeoShape(other.area, other.lines, _points);
        return ret;
    }

    @Override
    public Geom intersection(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
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
        if(getBounds().isDisjoint(other.getBounds(), accuracy)){
            return null;
        }
        VectList ret = new VectList(vects.size());
        VectBuilder vect = new VectBuilder();
        for (int i = 0; i < vects.size(); i++) {
            vects.getVect(i, vect);
            if (other.relate(vect, accuracy) != Relate.OUTSIDE) {
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
    public Geom less(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        PointSet ret = less(other, accuracy);
        return (ret == null) ? null : ret.simplify();
    }

    public PointSet less(Geom other, Tolerance accuracy) throws NullPointerException {
        if(getBounds().isDisjoint(other.getBounds(), accuracy)){
            return this;
        }
        VectList ret = new VectList(vects.size());
        VectBuilder vect = new VectBuilder();
        for (int i = 0; i < vects.size(); i++) {
            vects.getVect(i, vect);
            if (other.relate(vect, accuracy) == Relate.OUTSIDE) {
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

    static class NearLinkRemover implements SpatialNode.NodeProcessor<Line> {

        final double thresholdSq;
        final Network network;
        double x;
        double y;

        public NearLinkRemover(double threshold, Network network) {
            this.thresholdSq = threshold * threshold;
            this.network = network;
        }

        void reset(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean process(Rect bounds, Line j) {
            double jx = (j.ax + j.bx) / 2;
            double jy = (j.ay + j.by) / 2;
            if (Vect.distSq(x, y, jx, jy) < thresholdSq) {
                network.removeLink(j);
            }
            return true;
        }
    }
}
