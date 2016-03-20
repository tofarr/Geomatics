package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jg.util.RTree;
import org.jg.util.SpatialNode;
import org.jg.util.SpatialNode.NodeProcessor;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.VectList;
import org.jg.util.VectMap.VectMapProcessor;

/**
 *
 * @author tofar_000
 */
public class LineString implements Geom {

    public static final LineString[] EMPTY = new LineString[0];
    final VectList vects;
    SpatialNode<Line> lineIndex;

    LineString(VectList vects) {
        this.vects = vects;
    }

    /**
     * Get linestrings based on the ords given. LineStrings
     *
     * @param ords ordinates
     * @return a LineString
     * @throws IllegalArgumentException if an ordinate was infinite or NaN
     * @throws NullPointerException if ords was null
     */
    public static LineString[] valueOf(double... ords) throws IllegalArgumentException, NullPointerException {
        return valueOf(new VectList(ords));
    }

    /**
     * Get a linestring based on the list of vectors given. (Includes defensive copy)
     *
     * @param vects
     * @return a linestring, or null if the list of vectors was empty
     * @throws NullPointerException if vects was null
     */
    public static LineString[] valueOf(VectList vects) throws NullPointerException {
        if (vects.size() < 2) {
            return EMPTY;
        }
        Network network = new Network();
        network.addAllLinks(vects);
        List<VectList> ret = new ArrayList<>();
        network.extractLines(ret, false);
        if(ret.isEmpty()){
            return EMPTY;
        }
        LineString[] lines = ret.toArray(new LineString[ret.size()]);
        return lines;
    }
    
    public static LineString[] valueOf(Network network) throws NullPointerException {
        List<VectList> ret = new ArrayList<>();
        network.extractLines(ret, false);
        if(ret.isEmpty()){
            return EMPTY;
        }
        LineString[] lines = new LineString[ret.size()];
        for(int i = 0; i < lines.length; i++){
            lines[i] = new LineString(ret.get(i));
        }
        return lines;
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
        if (!transformed.isOrdered()) {
            transformed.reverse();
        }
        LineString ret = new LineString(transformed);
        return ret;
    }

    @Override
    public PathIterator pathIterator() {
        return new PathIterator() {
            int index = 0;

            @Override
            public int getWindingRule() {
                return WIND_NON_ZERO;
            }

            @Override
            public boolean isDone() {
                return (index >= vects.size());
            }

            @Override
            public void next() {
                if (index < vects.size()) {
                    index++;
                }
            }

            @Override
            public int currentSegment(float[] coords) {
                coords[0] = (float) vects.getX(0);
                coords[1] = (float) vects.getY(0);
                return (index == 0) ? SEG_MOVETO : SEG_LINETO;
            }

            @Override
            public int currentSegment(double[] coords) {
                coords[0] = vects.getX(0);
                coords[1] = vects.getY(0);
                return (index == 0) ? SEG_MOVETO : SEG_LINETO;
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
            for (int i = 0; i < vects.size(); i++) {
                appendable.append(", ").append(Vect.ordToStr(vects.getX(i))).append(',').append(Vect.ordToStr(vects.getY(i)));
            }
            appendable.append(']');
        } catch (IOException ex) {
            throw new GeomException("Error writing LineStirng", ex);
        }
    }

    public void addTo(Network network) throws NullPointerException {
        network.addAllLinks(vects);
    }

    @Override
    public GeoShape toGeoShape(Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        LineString[] array = new LineString[]{this};
        MultiLineString lines = new MultiLineString(array);
        return new GeoShape(null, lines, null, getBounds());
    }

    @Override
    public void addTo(Network network, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
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

    @Override
    public Geom buffer(double amt, Tolerance flatness, Tolerance accuracy) throws IllegalArgumentException, NullPointerException {
        Vect.check(amt, "Invalid amt {0}");
        if (amt < 0) {
            return null;
        } else if (amt == 0) {
            return this;
        } else if (vects.size() == 1) {
            return vects.getVect(0).buffer(amt, flatness, accuracy);
        }
        VectList buffer = bufferInternal(vects, amt, flatness, accuracy);
        Network network = new Network();
        network.addAllLinks(buffer);
        network.explicitIntersections(accuracy);
        removeNearLines(network, vects, amt);
        return Area.valueOfInternal(network, accuracy);
    }

    static void removeNearLines(Network network, VectList lines, double threshold) {
        final SpatialNode<Line> lineIndex = network.getLinks();
        final NearLinkRemover remover = new NearLinkRemover(threshold, network);
        int index = lines.size() - 1;
        RectBuilder bounds = new RectBuilder();
        while (index-- > 0) {
            Line i = lines.getLine(index);
            bounds.reset().addInternal(i.ax, i.ay).addInternal(i.bx, i.by).buffer(threshold);
            lineIndex.forOverlapping(bounds.build(), remover);
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
    public Relate relate(Vect vect, Tolerance accuracy) throws NullPointerException {
        return relateInternal(vect.x, vect.y, accuracy);
    }

    @Override
    public Relate relate(VectBuilder vect, Tolerance accuracy) throws NullPointerException {
        return relateInternal(vect.getX(), vect.getY(), accuracy);
    }

    Relate relateInternal(double x, double y, Tolerance accuracy) {
        RelateProcessor processor = new RelateProcessor(x, y, accuracy);
        return getLineIndex().forInteracting(Rect.valueOf(x, y, x, y), processor) ? Relate.OUTSIDE : Relate.TOUCH;
    }
    
    public MultiLineString toMultiLineString(){
        return new MultiLineString(new LineString[]{this});
    }

    @Override
    public Geom union(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        if (other instanceof LineString) {
            return union((LineString) other, accuracy);
        } else if (other instanceof MultiLineString) {
            return union((MultiLineString)other, accuracy);
        } else {
            return union(other.toGeoShape(flatness, accuracy), accuracy);
        }
    }

    public Geom union(LineString other, Tolerance accuracy) throws NullPointerException {
        if (getBounds().isDisjoint(other.getBounds(), accuracy)) {
            LineString[] lines = new LineString[]{this, other};
            Arrays.sort(lines, COMPARATOR);
            return new MultiLineString(lines);
        }
        Network network = new Network();
        addTo(network);
        other.addTo(network);
        network.explicitIntersections(accuracy);
        LineString[] lineStrings = valueOf(network);
        return (lineStrings.length == 1) ? lineStrings[0] : new MultiLineString(lineStrings);
    }
    
    public MultiLineString union(MultiLineString other, Tolerance accuracy) throws NullPointerException {
        if (getBounds().isDisjoint(other.getBounds(), accuracy)) {
            LineString[] lines = new LineString[other.lineStrings.length+1];
            lines[0] = this;
            System.arraycopy(other.lineStrings, 0, lines, 1, other.lineStrings.length);
            Arrays.sort(lines, COMPARATOR);
            return new MultiLineString(lines);
        }
        Network network = new Network();
        addTo(network);
        other.addTo(network);
        network.explicitIntersections(accuracy);
        return new MultiLineString(valueOf(network));
    }

    public GeoShape union(GeoShape other, Tolerance accuracy) throws NullPointerException {
        return toMultiLineString().union(other, accuracy);
    }

    @Override
    public Geom intersection(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        if (getBounds().isDisjoint(other.getBounds(), accuracy)) {
            return null;
        }
        return toMultiLineString().intersection(other.toGeoShape(flatness, accuracy), flatness, accuracy);
    }

    @Override
    public Geom less(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        if (getBounds().isDisjoint(other.getBounds(), accuracy)) {
            return this;
        }
        return toMultiLineString().less(other.toGeoShape(flatness, accuracy), flatness, accuracy);
    }

    public Vect getPoint(int index) throws IndexOutOfBoundsException {
        return vects.getVect(index);
    }

    public VectBuilder getPoint(int index, VectBuilder target) throws IndexOutOfBoundsException, NullPointerException {
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
