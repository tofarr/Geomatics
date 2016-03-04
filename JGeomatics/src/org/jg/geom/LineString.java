package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.DataInput;
import java.io.DataOutput;
import org.jg.util.Network;
import org.jg.util.RTree;
import org.jg.util.SpatialNode.NodeProcessor;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.VectList;

/**
 *
 * @author tofar_000
 */
public class LineString implements Geom {

    private final VectList vects;
    private RTree<Line> lineIndex;

    LineString(VectList vects) {
        this.vects = vects;
    }
    
    /**
     * Get a linestring based on the ords given
     * @param ords ordinates
     * @return a LineString
     * @throws IllegalArgumentException if an ordinate was infinite or NaN
     * @throws NullPointerException if ords was null
     */
    public static LineString valueOf(double... ords) throws IllegalArgumentException, NullPointerException{
        return valueOf(new VectList(ords));
    }

    /**
     * Get a linestring based on the list of vectors given. (Includes defensive
     * copy)
     *
     * @param vects
     * @return a linestring, or null if the list of vectors was empty
     * @throws NullPointerException if vects was null
     */
    public static LineString valueOf(VectList vects) throws NullPointerException {
        if (vects.isEmpty()) {
            return null;
        }
        vects = vects.clone();
        int index = vects.size() - 1;
        double bx = vects.getX(index);
        double by = vects.getY(index);
        while (index-- > 0) {
            double ax = vects.getX(index);
            double ay = vects.getY(index);
            if ((ax == bx) && (ay == by)) {
                vects.remove(index);
            }
        }
        return new LineString(vects);
    }

    @Override
    public Rect getBounds() {
        return vects.getBounds();
    }

    @Override
    public void addBoundsTo(RectBuilder target) throws NullPointerException {
        target.add(vects.getBounds());
    }

    @Override
    public LineString transform(Transform transform) throws NullPointerException {
        if (transform.mode == Transform.NO_OP) {
            return this;
        }
        VectList transformed = vects.clone();
        transformed.transform(transform);
        return new LineString(transformed);
    }

    @Override
    public PathIterator pathIterator() {
        final VectList v = (vects.size() == 1) ? new VectList().add(vects, 0).add(vects, 0) : vects;
        return new PathIterator() {
            int index = 0;

            @Override
            public int getWindingRule() {
                return WIND_NON_ZERO;
            }

            @Override
            public boolean isDone() {
                return (index >= v.size());
            }

            @Override
            public void next() {
                if (index < v.size()) {
                    index++;
                }
            }

            @Override
            public int currentSegment(float[] coords) {
                if (index == 0) {
                    coords[0] = (float) v.getX(0);
                    coords[1] = (float) v.getY(0);
                    return SEG_MOVETO;
                } else {
                    coords[0] = (float) v.getX(index);
                    coords[1] = (float) v.getY(index);
                    return SEG_LINETO;
                }
            }

            @Override
            public int currentSegment(double[] coords) {
                if (index == 0) {
                    coords[0] = v.getX(0);
                    coords[1] = v.getY(0);
                    return SEG_MOVETO;
                } else {
                    coords[0] = v.getX(index);
                    coords[1] = v.getY(index);
                    return SEG_LINETO;
                }
            }

        };
    }

    @Override
    public LineString clone() {
        return this;
    }

    @Override
    public void toString(Appendable appendable) throws NullPointerException, GeomException {
        vects.toString(appendable);
    }

    @Override
    public void addTo(Network network, Tolerance tolerance) throws NullPointerException, IllegalArgumentException {
        network.addAllLinks(vects);
    }

    public double getLength() {
        return getLength(vects);
    }

    static double getLength(VectList vects) {
        double ret = 0;
        if (!vects.isEmpty()) {
            int i = vects.size() - 1;
            double bx = vects.getX(i);
            double by = vects.getY(i);
            while (--i >= 0) {
                double ax = vects.getX(i);
                double ay = vects.getY(i);
                ret += Math.sqrt(Vect.distSq(ax, ay, bx, by));
                bx = ax;
                by = ay;
            }
        }
        return ret;
    }

    public boolean isValid(Tolerance tolerance) {
        if (vects.size() < 2) {
            return false;
        }

        //Remove any duplicate points
        int index = vects.size() - 1;
        double bx = vects.getX(index);
        double by = vects.getY(index);
        while (index-- > 0) {
            double ax = vects.getX(index);
            double ay = vects.getY(index);
            if (tolerance.match(ax, ay, bx, by)) {
                return false;
            }
            bx = ax;
            by = ay;
        }

        return true;
    }

    /**
     * Remove any colinear vertices 
     * and make sure ordered by lowest first
     * @param tolerance
     * @return
     */
    public LineString normalize(Tolerance tolerance) {

        boolean changed = false;
        VectList normalized = vects.clone();

        //Remove any colinear points
        double toleranceSq = tolerance.getTolerance();
        toleranceSq *= toleranceSq;
        for (int c = normalized.size() - 1, b = c - 1, a = b - 1; a >= 0; a--, b--, c--) {
            double ax = normalized.getX(a);
            double ay = normalized.getY(a);
            double bx = normalized.getX(b);
            double by = normalized.getY(b);
            double cx = normalized.getX(c);
            double cy = normalized.getY(c);
            if (Line.distSegVectSq(ax, ay, cx, cy, bx, by) <= toleranceSq) {
                normalized.remove(b);
                changed = true;
            }
        }

        if (!normalized.isOrdered()) {
            normalized.reverse();
            changed = true;
        }

        return changed ? new LineString(normalized) : this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LineString) {
            LineString lineString = (LineString) obj;
            return vects.equals(lineString.vects);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + vects.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return vects.toString();
    }

    RTree<Line> getLineIndex() {
        RTree<Line> ret = lineIndex;
        if (ret == null) {
            Rect[] bounds = new Rect[vects.size() - 1];
            Line[] lines = new Line[bounds.length];
            int index = bounds.length;
            double bx = vects.getX(index);
            double by = vects.getY(index);
            while (index-- > 0) {
                double ax = vects.getX(index);
                double ay = vects.getY(index);
                bounds[index] = Rect.valueOf(ax, ay, bx, by);
                lines[index] = Line.valueOf(ax, ay, bx, by);
                bx = ax;
                by = ay;
            }
            ret = new RTree<>(bounds, lines);
            lineIndex = ret;
        }
        return ret;
    }

    public boolean getInteractingLines(Rect bounds, final NodeProcessor<Line> lineProcessor) {
        return getLineIndex().forInteracting(bounds, lineProcessor);
    }

    public boolean getOverlappingLines(Rect bounds, final NodeProcessor<Line> lineProcessor) {
        return getLineIndex().forOverlapping(bounds, lineProcessor);
    }

    public static LineString read(DataInput in) throws NullPointerException, IllegalArgumentException, GeomException {
        VectList vects = VectList.read(in);
        return valueOf(vects);
    }

    public void write(DataOutput out) throws NullPointerException, GeomException {
        vects.write(out);
    }

    @Override
    public Geom buffer(double amt, Tolerance tolerance) throws IllegalArgumentException, NullPointerException {
        Vect.check(amt, "Invalid amt {0}");
        if (amt < 0) {
            return null;
        } else if (amt == 0) {
            return this;
        } else if (vects.size() == 1) {
            return vects.getVect(0).buffer(amt, tolerance);
        }

        VectList buffer = bufferInternal(vects, amt, tolerance);

        //add to network
        //remove any point from network which is too close to a line from this
        throw new UnsupportedOperationException("Not yet implemented");

    }

    //The buffer produced by this may be self overlapping, and will need to be cleaned in a network before use
    static VectList bufferInternal(VectList vects, double amt, Tolerance tolerance) {
        VectList result = new VectList(vects.size() << 2);
        VectBuilder vect = new VectBuilder();

        double ax = vects.getX(0);
        double ay = vects.getY(0);
        double bx = vects.getX(1);
        double by = vects.getY(1);

        //draw arc on end of line string
        Line.projectOutward(ax, ay, bx, by, 0, -amt, tolerance, vect);
        double ix = vect.getX();
        double iy = vect.getY();
        Line.projectOutward(ax, ay, bx, by, 0, amt, tolerance, vect);
        Vect.linearizeArc(ax, ay, ix, iy, vect.getX(), vect.getY(), Math.abs(amt), tolerance.getTolerance(), result);

        int index = 1;
        while (++index < vects.size()) {
            double cx = vects.getX(index);
            double cy = vects.getY(index);
            projectOutward(ax, ay, bx, by, cx, cy, amt, tolerance, vect, result);
            ax = bx;
            bx = cx;
            ay = by;
            by = cy;
        }

        //draw arc on end of line string
        Line.projectOutward(ax, ay, bx, by, 1, amt, tolerance, vect);
        ix = vect.getX();
        iy = vect.getY();
        Line.projectOutward(bx, by, ax, ay, 0, amt, tolerance, vect);
        Vect.linearizeArc(bx, by, ix, iy, vect.getX(), vect.getY(), Math.abs(amt), tolerance.getTolerance(), result);

        double tmp = ax;
        ax = bx;
        bx = tmp;
        tmp = ay;
        ay = by;
        by = tmp;
        index = vects.size() - 2;

        while (--index >= 0) {
            double cx = vects.getX(index);
            double cy = vects.getY(index);
            projectOutward(ax, ay, bx, by, cx, cy, amt, tolerance, vect, result);
            ax = bx;
            bx = cx;
            ay = by;
            by = cy;
        }

        Line.projectOutward(ax, ay, bx, by, 0, amt, tolerance, vect);
        result.add(vect);

        return result;
    }

    static void projectOutward(double ax, double ay, double bx, double by, double cx, double cy, double amt, Tolerance tolerance, VectBuilder work, VectList result) {
        if (Line.counterClockwise(ax, ay, cx, cy, bx, by) >= 0) { //if angle abc is acute, then this is easy - no linearize needed
            //project outward distance from angle
            double dx = ((bx - ax) + (bx - cx)) / 2;
            double dy = ((by - ay) + (by - cy)) / 2;
            double mul = amt / Math.sqrt((dx * dx) + (dy * dy));
            dx *= mul;
            dy *= mul;
            result.add(bx + dx, by + dy);
        } else {
            Line.projectOutward(ax, ay, bx, by, 1, amt, tolerance, work);
            double ix = work.getX();
            double iy = work.getY();
            Line.projectOutward(bx, by, cx, cy, 0, amt, tolerance, work);
            Vect.linearizeArc(bx, by, ix, iy, work.getX(), work.getY(), Math.abs(amt), tolerance.getTolerance(), result);
        }
    }

    @Override
    public Relate relate(Vect vect, Tolerance tolerance) throws NullPointerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Relate relate(VectBuilder vect, Tolerance tolerance) throws NullPointerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    

    public Vect getVect(int index) throws IndexOutOfBoundsException {
        return vects.getVect(index);
    }

    public VectBuilder getVect(int index, VectBuilder target) throws IndexOutOfBoundsException, NullPointerException {
        return vects.getVect(index, target);
    }

    public Line getLine(int index) throws IndexOutOfBoundsException, NullPointerException {
        return vects.getLine(index);
    }

    public double getX(int index) throws IndexOutOfBoundsException {
        return vects.getX(index);
    }

    public double getY(int index) {
        return vects.getY(index);
    }

    public int numVects() {
        return vects.size();
    }

    public int numLines() {
        return Math.max(vects.size() - 1, 0);
    }

    public boolean isEmpty() {
        return vects.isEmpty();
    }

}
