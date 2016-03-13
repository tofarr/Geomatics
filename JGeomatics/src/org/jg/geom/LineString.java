package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jg.util.RTree;
import org.jg.util.SpatialNode;
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
    private SpatialNode<Line> lineIndex;

    LineString(VectList vects) {
        this.vects = vects;
    }

    /**
     * Get a linestring based on the ords given
     *
     * @param ords ordinates
     * @return a LineString
     * @throws IllegalArgumentException if an ordinate was infinite or NaN
     * @throws NullPointerException if ords was null
     */
    public static LineString valueOf(double... ords) throws IllegalArgumentException, NullPointerException {
        return valueOf(new VectList(ords));
    }

    /**
     * Get a linestring based on the list of vectors given. (Includes defensive copy)
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
            bx = ax;
            by = ay;
        }
        return new LineString(vects);
    }

    @Override
    public Rect getBounds() {
        return vects.getBounds();
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
        try {
            appendable.append("[\"LS\"");
            for(int i = 0; i < vects.size(); i++){
                appendable.append(", ").append(Vect.ordToStr(vects.getX(i))).append(',').append(Vect.ordToStr(vects.getY(i)));
            }
            appendable.append(']');
        } catch (IOException ex) {
           throw new GeomException("Error writing LineStirng", ex);
        }
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
     * Remove any colinear vertices and make sure ordered by lowest first
     *
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
        StringBuilder str = new StringBuilder();
        toString(str);
        return str.toString();
    }

    public SpatialNode<Line> getLineIndex() {
        SpatialNode<Line> ret = lineIndex;
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
            RTree<Line> tree = new RTree<>(bounds, lines);
            ret = tree.getRoot();
            lineIndex = ret;
        }
        return ret;
    }

    public boolean forInteractingLines(Rect bounds, final NodeProcessor<Line> lineProcessor) {
        return getLineIndex().forInteracting(bounds, lineProcessor);
    }

    public boolean forOverlappingLines(Rect bounds, final NodeProcessor<Line> lineProcessor) {
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
    public Geom buffer(double amt, Tolerance flatness, Tolerance tolerance) throws IllegalArgumentException, NullPointerException {
        Vect.check(amt, "Invalid amt {0}");
        if (amt < 0) {
            return null;
        } else if (amt == 0) {
            return this;
        } else if (vects.size() == 1) {
            return vects.getVect(0).buffer(amt, flatness, tolerance);
        }

        VectList buffer = bufferInternal(vects, amt, flatness, tolerance);

        //Since the buffer may self intersect, we need to identify points of self intersection
        Network network = new Network();
        network.addAllLinks(buffer);

        network.explicitIntersections(tolerance);

        removeWithinBuffer(vects, network, amt, flatness, tolerance);

        return RingSet.valueOf(network);
    }
    
    //remove any link from network with a mid point closer than the amt to one of the lines in this
    static void removeWithinBuffer(VectList vects, Network network, double amt, Tolerance flatness, Tolerance tolerance){
        System.out.println("Should pick min then remove by crossing points");
        System.out.println("Or could convert to ringsset but throw away holes - WONT WORK");
        System.out.println("or we fix threshold");
        final SpatialNode<Line> lines = network.getLinks();
        double threshold = amt - (flatness.tolerance * 2);
        final NearLinkRemover remover = new NearLinkRemover(threshold, network);
        int index = vects.size() - 1;
        RectBuilder bounds = new RectBuilder();
        double bx = vects.getX(index);
        double by = vects.getY(index);
        index--;
        while (index-- > 0) {
            double ax = vects.getX(index);
            double ay = vects.getY(index);
            index--;
            bounds.reset().add(ax, ay).add(bx, by);
            bounds.buffer(threshold);
            remover.reset(ax,ay,bx,by);
            lines.forInteracting(bounds.build(), remover);
            bx = ax;
            by = ay;
        }
    }

    //The buffer produced by this may be self overlapping, and will need to be cleaned in a network before use
    static VectList bufferInternal(VectList vects, double amt, Tolerance flatness, Tolerance tolerance) {
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
        Vect.linearizeArc(ax, ay, ix, iy, vect.getX(), vect.getY(), Math.abs(amt), flatness.getTolerance(), result);

        int index = 1;
        while (++index < vects.size()) {
            double cx = vects.getX(index);
            double cy = vects.getY(index);
            projectOutward(ax, ay, bx, by, cx, cy, amt, flatness, tolerance, vect, result);
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
        Vect.linearizeArc(bx, by, ix, iy, vect.getX(), vect.getY(), Math.abs(amt), flatness.getTolerance(), result);

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
            projectOutward(ax, ay, bx, by, cx, cy, amt, flatness, tolerance, vect, result);
            ax = bx;
            bx = cx;
            ay = by;
            by = cy;
        }

        Line.projectOutward(ax, ay, bx, by, 1, amt, tolerance, vect);
        result.add(vect);

        return result;
    }

    static void projectOutward(double ax, double ay, double bx, double by, double cx, double cy, double amt, Tolerance flatness, Tolerance tolerance, VectBuilder work, VectList result) {
        if (Line.counterClockwise(ax, ay, cx, cy, bx, by) <= 0) { //if angle abc is acute, then this is easy - no linearize needed
            Line.projectOutward(ax, ay, bx, by, 1, amt, tolerance, work);
            result.add(work);
            Line.projectOutward(bx, by, cx, cy, 0, amt, tolerance, work);
            result.add(work);            
        } else {
            Line.projectOutward(ax, ay, bx, by, 1, amt, tolerance, work);
            double ix = work.getX();
            double iy = work.getY();
            Line.projectOutward(bx, by, cx, cy, 0, amt, tolerance, work);
            Vect.linearizeArc(bx, by, ix, iy, work.getX(), work.getY(), Math.abs(amt), flatness.getTolerance(), result);
        }
    }

    @Override
    public Relate relate(Vect vect, Tolerance tolerance) throws NullPointerException {
        RelateProcessor processor = new RelateProcessor(vect.x, vect.y, tolerance);
        return getLineIndex().forInteracting(vect.getBounds(), processor) ? Relate.OUTSIDE : Relate.TOUCH;
    }

    @Override
    public Relate relate(VectBuilder vect, Tolerance tolerance) throws NullPointerException {
        double x = vect.getX();
        double y = vect.getY();
        RelateProcessor processor = new RelateProcessor(x, y, tolerance);
        return getLineIndex().forInteracting(Rect.valueOf(x, y, x, y), processor) ? Relate.OUTSIDE : Relate.TOUCH;
    }

    @Override
    public Geom union(Geom other, Tolerance flatness, Tolerance tolerance) throws NullPointerException {
        if(getBounds().buffer(tolerance.tolerance).isDisjoint(other.getBounds())){
            return GeomSet.normalizedValueOf(this, other);
        }else{
            return Network.intersection(flatness, tolerance, this, other);
        }
        
    }

    @Override
    public Geom intersection(Geom other, Tolerance flatness, Tolerance tolerance) throws NullPointerException {
        if(getBounds().buffer(tolerance.tolerance).isDisjoint(other.getBounds())){
            return null;
        }else{
            return Network.intersection(flatness, tolerance, this, other);
        }
    }

    @Override
    public Geom less(Geom other, Tolerance flatness, Tolerance tolerance) throws NullPointerException {
        if(getBounds().buffer(tolerance.tolerance).isDisjoint(other.getBounds())){
            return this;
        }else{
            return Network.less(flatness, tolerance, this, other);
        }
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

    static class NearLinkRemover implements NodeProcessor<Line> {

        final double thresholdSq;
        final Network network;
        double iax;
        double iay;
        double ibx;
        double iby;

        public NearLinkRemover(double threshold, Network network) {
            this.thresholdSq = threshold * threshold;
            this.network = network;
        }

        void reset(double iax, double iay, double ibx, double iby) {
            this.iax = iax;
            this.iay = iay;
            this.ibx = ibx;
            this.iby = iby;
        }

        @Override
        public boolean process(Rect bounds, Line j) {
            double x = (j.ax + j.bx) / 2;
            double y = (j.ay + j.by) / 2;
            if (Line.distSegVectSq(iax, iay, ibx, iby, x, y) < thresholdSq) {
                network.removeLink(j);
            }
            return true;
        }
    }

    static class RelateProcessor implements NodeProcessor<Line> {

        final double x;
        final double y;
        final double toleranceSq;

        public RelateProcessor(double x, double y, Tolerance tolerance) {
            this.x = x;
            this.y = y;
            toleranceSq = tolerance.tolerance * tolerance.tolerance;
        }

        @Override
        public boolean process(Rect bounds, Line value) {
            double distSq = Line.distSegVectSq(value.ax, value.ay, value.bx, value.by, x, y);
            return (distSq > toleranceSq);
        }
    }
}
