package org.jg.geom;

import java.awt.geom.PathIterator;
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

/**
 * Immutable 2D LineString. Checks are in place so that ordinates are not infinite or NaN, that
 * duplicate sequential coordinates are not present, and that LineStrings do not self intersect
 * (except at the start and end point, which may be the same) Line strings are always ordered such
 * that the end point with the lower natural ordering is first. (Or the next point along if line is
 * a ring)
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
     * Get a linestring based on the ords given. (Returns array since may be split if self
     * intersecting)
     *
     * @param accuracy
     * @param ords ordinates
     * @return a LineString
     * @throws IllegalArgumentException if an ordinate was infinite or NaN, or linestring was self
     * intersecting or empty
     * @throws NullPointerException if ords or accuracy was null
     */
    public static LineString valueOf(Tolerance accuracy, double... ords)
            throws IllegalArgumentException, NullPointerException {
        return valueOf(accuracy, new VectList(ords));
    }

    /**
     * Get a linestring based on the ords given. (Returns array since may be split if self
     * intersecting)
     *
     * @param accuracy
     * @param vects vectors
     * @return a LineString
     * @throws IllegalArgumentException if an ordinate was infinite or NaN, or linestring was self
     * intersecting or empty
     * @throws NullPointerException if vects or accuracy was null
     */
    public static LineString valueOf(Tolerance accuracy, VectList vects)
            throws IllegalArgumentException, NullPointerException {
        LineString[] ret = LineString.parseAll(accuracy, vects);
        if (ret.length != 1) {
            throw new IllegalArgumentException("Ordinates must be one non self intersecting line : " + vects);
        }
        return ret[0];
    }

    /**
     * Get linestrings based on the list of vectors given. (Returns array since may be split if self
     * intersecting) copy)
     *
     * @param accuracy
     * @param ords
     * @return an array of line strings
     * @throws NullPointerException if ords or accuracy was null
     * @throws IllegalArgumentException
     */
    public static LineString[] parseAll(Tolerance accuracy, double... ords) throws NullPointerException, IllegalArgumentException {
        return parseAll(accuracy, new VectList(ords));
    }
    
    /**
     * Get linestrings based on the list of vectors given. (Returns array since may be split if self
     * intersecting) copy)
     *
     * @param accuracy
     * @param vects
     * @return an array of linestring
     * @throws NullPointerException if vects or accuracy was null
     */
    public static LineString[] parseAll(Tolerance accuracy, VectList vects) throws NullPointerException {
        if (vects.size() < 2) {
            return EMPTY;
        }
        Network network = new Network();
        network.addAllLinks(vects);
        network.explicitIntersections(accuracy);
        network.snap(accuracy);
        List<VectList> ret = new ArrayList<>();
        network.extractLines(ret, false);
        if (ret.isEmpty()) {
            return EMPTY;
        }
        LineString[] lines = new LineString[ret.size()];
        for (int i = 0; i < lines.length; i++) {
            lines[i] = new LineString(ret.get(i));
        }
        return lines;
    }

    /**
     * Extract line strings from the network given
     *
     * @param accuracy
     * @param network
     * @return array of line strings
     * @throws NullPointerException if network or tolerance was null
     */
    public static LineString[] parseAll(Tolerance accuracy, Network network) throws NullPointerException {
        network = network.clone();
        network.explicitIntersections(accuracy);
        network.snap(accuracy);
        return parseAllInternal(network);
    }

    static LineString[] parseAllInternal(Network network) throws NullPointerException {
        List<VectList> ret = new ArrayList<>();
        network.extractLines(ret, false);
        if (ret.isEmpty()) {
            return EMPTY;
        }
        LineString[] lines = new LineString[ret.size()];
        for (int i = 0; i < lines.length; i++) {
            VectList vects = ret.get(i);
            int end = vects.size()-1;
            if((vects.getX(0) == vects.getX(end)) && (vects.getY(0) == vects.getY(end))){
                int index = Ring.minIndex(vects);
                if(index != 0){
                    vects = Ring.rotate(vects, index);
                }
                if(Ring.getArea(vects) < 0){
                    vects.reverse();
                }
            }
            lines[i] = new LineString(vects);
        }
        Arrays.sort(lines, COMPARATOR);
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
        int end = transformed.size()-1;
        if((transformed.getX(0) == transformed.getX(end)) && (transformed.getY(0) == transformed.getY(end))){
            int index = Ring.minIndex(transformed);
            if(index != 0){
                transformed = Ring.rotate(transformed, index);
            }
            if(Ring.getArea(transformed) < 0){
                transformed.reverse();
            }
        }else if(!transformed.isOrdered()){
            transformed.reverse();
        }
        LineString ret = new LineString(transformed);
        return ret;
    }

    /**
     * Attempt to get a simplified version of this LineString. If it has only 2 points, return a
     * line, otherwise return this.
     *
     * @return
     */
    public Geom simplify() {
        return (vects.size() == 2) ? vects.getLine(0) : this;
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
                coords[0] = (float) vects.getX(index);
                coords[1] = (float) vects.getY(index);
                return (index == 0) ? SEG_MOVETO : SEG_LINETO;
            }

            @Override
            public int currentSegment(double[] coords) {
                coords[0] = vects.getX(index);
                coords[1] = vects.getY(index);
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

    @Override
    public GeoShape toGeoShape(Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        LineString[] array = new LineString[]{this};
        LineSet lines = new LineSet(array);
        return new GeoShape(null, lines, null, getBounds());
    }

    @Override
    public void addTo(Network network, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        addTo(network);
    }

    /**
     * Add this linestring to the network given
     *
     * @param network
     * @throws NullPointerException
     */
    public void addTo(Network network) throws NullPointerException {
        network.addAllLinks(vects);
    }

    /**
     * Get the total length of this line string
     *
     * @return
     */
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

    /**
     * Get an index of the lines in this line string
     *
     * @return
     */
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

    /**
     * Alias for getLineIndex().forInteracting(bounds, lineProcessor)
     *
     * @param bounds
     * @param lineProcessor
     * @return
     */
    public boolean forInteractingLines(Rect bounds, final NodeProcessor<Line> lineProcessor) {
        return getLineIndex().forInteracting(bounds, lineProcessor);
    }

    /**
     * Alias for getLineIndex().forInteracting(bounds, lineProcessor)
     *
     * @param bounds
     * @param lineProcessor
     * @return
     */
    public boolean forOverlappingLines(Rect bounds, final NodeProcessor<Line> lineProcessor) {
        return getLineIndex().forOverlapping(bounds, lineProcessor);
    }

    @Override
    public Geom buffer(double amt, Linearizer linearizer, Tolerance accuracy) throws IllegalArgumentException, NullPointerException {
        Vect.check(amt, "Invalid amt {0}");
        if (amt < 0) {
            return null;
        } else if (amt == 0) {
            return this;
        }
        VectList buffer = bufferInternal(vects, amt, linearizer, accuracy);
        return Ring.buildGeomFromRing(buffer, accuracy);
    }

    //The buffer produced by this may be self overlapping, and will need to be cleaned in a network before use
    static VectList bufferInternal(VectList vects, double amt, Linearizer linearizer, Tolerance tolerance) {
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
        result.add(ix, iy);
        Line.projectOutward(ax, ay, bx, by, 0, amt, tolerance, vect);
        linearizer.linearizeSegment(ax, ay, ix, iy, vect.getX(), vect.getY(), result);

        int index = 1;
        while (++index < vects.size()) {
            double cx = vects.getX(index);
            double cy = vects.getY(index);
            projectOutward(ax, ay, bx, by, cx, cy, amt, linearizer, tolerance, vect, result);
            ax = bx;
            bx = cx;
            ay = by;
            by = cy;
        }

        //draw arc on end of line string
        Line.projectOutward(ax, ay, bx, by, 1, amt, tolerance, vect);
        ix = vect.getX();
        iy = vect.getY();
        result.add(ix, iy);
        Line.projectOutward(bx, by, ax, ay, 0, amt, tolerance, vect);
        linearizer.linearizeSegment(bx, by, ix, iy, vect.getX(), vect.getY(), result);

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
            projectOutward(ax, ay, bx, by, cx, cy, amt, linearizer, tolerance, vect, result);
            ax = bx;
            bx = cx;
            ay = by;
            by = cy;
        }

        Line.projectOutward(ax, ay, bx, by, 1, amt, tolerance, vect);
        result.add(vect);

        return result;
    }

    static void projectOutward(double ax, double ay, double bx, double by, double cx, double cy, double amt, Linearizer linearizer, Tolerance tolerance, VectBuilder work, VectList result) {
        if(amt > 0){
            if (Line.counterClockwise(ax, ay, cx, cy, bx, by, tolerance) <= 0) { //if angle abc is acute, then this is easy - no linearize needed
                Line.projectOutward(ax, ay, bx, by, 1, amt, tolerance, work);
                result.add(work);
                Line.projectOutward(bx, by, cx, cy, 0, amt, tolerance, work);
                result.add(work);
            } else {
                Line.projectOutward(ax, ay, bx, by, 1, amt, tolerance, work);
                double ix = work.getX();
                double iy = work.getY();
                result.add(ix, iy);
                Line.projectOutward(bx, by, cx, cy, 0, amt, tolerance, work);
                linearizer.linearizeSegment(bx, by, ix, iy, work.getX(), work.getY(), result);
            }
        }else{
            if (Line.counterClockwise(ax, ay, cx, cy, bx, by, tolerance) <= 0) { //if angle abc is acute, then this is easy - no linearize needed
                Line.projectOutward(ax, ay, bx, by, 1, amt, tolerance, work);
                double ix = work.getX();
                double iy = work.getY();
                Line.projectOutward(bx, by, cx, cy, 0, amt, tolerance, work);
                VectList arc = new VectList();
                arc.add(work);
                linearizer.linearizeSegment(bx, by, work.getX(), work.getY(), ix, iy, arc);
                arc.reverse();
                result.addAll(arc);
            } else { //if angle abc is obtuse, then this is easy - no linearize needed
                Line.projectOutward(ax, ay, bx, by, 1, amt, tolerance, work);
                result.add(work);
                Line.projectOutward(bx, by, cx, cy, 0, amt, tolerance, work);
                result.add(work);
            }
        }
    }

    @Override
    public int relate(Vect vect, Tolerance accuracy) throws NullPointerException {
        return relateInternal(vect.x, vect.y, accuracy);
    }

    @Override
    public int relate(VectBuilder vect, Tolerance accuracy) throws NullPointerException {
        return relateInternal(vect.getX(), vect.getY(), accuracy);
    }

    int relateInternal(double x, double y, Tolerance accuracy) {
        RelateProcessor processor = new RelateProcessor(x, y, accuracy);
        return Relation.A_OUTSIDE_B | (getLineIndex().forInteracting(Rect.valueOf(x, y, x, y), processor)
                ? Relation.B_OUTSIDE_A : Relation.TOUCH);
    }

    @Override
    public int relate(Geom geom, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        return NetworkRelationProcessor.relate(this, geom, linearizer, accuracy);
    }
    
    /**
     * Create a LineSet based on this lineString
     *
     * @return
     */
    public LineSet toLineSet() {
        return new LineSet(new LineString[]{this});
    }

    @Override
    public Geom union(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        return toLineSet().union(other, linearizer, accuracy);
    }

    @Override
    public Geom intersection(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        return toLineSet().intersection(other, linearizer, accuracy);
    }

    @Override
    public Geom less(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        LineSet ret = toLineSet().less(other, linearizer, accuracy);
        return (ret == null) ? null : ret.simplify();
    }
    
    @Override
    public double getArea(Linearizer linearizer, Tolerance accuracy){
        return 0;
    }
    
    /**
     * Get the point at the index given
     *
     * @param index
     * @return vector
     * @throws IndexOutOfBoundsException if index was out of bounds
     */
    public Vect getPoint(int index) throws IndexOutOfBoundsException {
        return vects.getVect(index);
    }

    /**
     * Get the point at the index given
     *
     * @param index
     * @param target target point
     * @return target
     * @throws IndexOutOfBoundsException if index is out of bounds
     * @throws NullPointerException if target was null
     */
    public VectBuilder getPoint(int index, VectBuilder target) throws IndexOutOfBoundsException, NullPointerException {
        return vects.getVect(index, target);
    }

    /**
     * Get the line at the index given
     *
     * @param index
     * @return target
     * @throws IndexOutOfBoundsException if index is out of bounds
     */
    public Line getLine(int index) throws IndexOutOfBoundsException, NullPointerException {
        return vects.getLine(index);
    }

    /**
     * Get x value at the index given
     *
     * @param index
     * @return
     * @throws IndexOutOfBoundsException if index is out of bounds
     */
    public double getX(int index) throws IndexOutOfBoundsException {
        return vects.getX(index);
    }

    /**
     * Get y value at the index given
     *
     * @param index
     * @return
     * @throws IndexOutOfBoundsException if index is out of bounds
     */
    public double getY(int index) throws IndexOutOfBoundsException {
        return vects.getY(index);
    }

    /**
     * Get the number of Points in this lineString
     *
     * @return
     */
    public int numPoints() {
        return vects.size();
    }

    /**
     * Get the number of Lines in this lineString
     *
     * @return
     */
    public int numLines() {
        return Math.max(vects.size() - 1, 0);
    }
    
    /**
     * Exclude any colinear vertices from this linestring
     * @param accuracy
     * @return
     */
    public LineString excludeColinear(Tolerance accuracy){
        VectList out = excludeColinear(vects, accuracy);
        if(out.size() == vects.size()){
            return this; // no colinear vertices
        }
        return new LineString(out);
    }
    
    public static VectList excludeColinear(VectList input, Tolerance accuracy){
        if(input.size() < 3){
            return input.clone();
        }
        final double tol = accuracy.tolerance * accuracy.tolerance;
        VectList output = new VectList();
        double ax = input.getX(0);
        double ay = input.getY(0);
        output.add(ax, ay);
        double bx = input.getX(1);
        double by = input.getY(1);
        for(int i = 2; i < input.size(); i++){
            double cx = input.getX(i);
            double cy = input.getY(i);
            if(Line.distSegVectSq(ax, ay, cx, cy, bx, by) > tol){
                output.add(bx, by);
                ax = bx;
                ay = by;
            }
            bx = cx;
            by = cy;
        }
        output.add(bx, by);
        return output;
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
