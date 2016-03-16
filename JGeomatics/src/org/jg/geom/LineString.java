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

    private final VectList vects;
    private SpatialNode<Line> lineIndex;
    private Boolean normalized;

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
        if (vects.size() < 2) {
            return null;
        }
        vects = vects.clone();
        removeDuplicates(vects);
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
        LineString ret = new LineString(transformed);
        ret.normalized = normalized;
        return ret;
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

    public void addTo(Network network) throws NullPointerException {
        network.addAllLinks(vects);
    }
    
    @Override
    public GeoShape toGeoShape(Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        List<LineString> lineList = splitOnSelfIntersect(accuracy);
        LineString[] lines = lineList.toArray(new LineString[lineList.size()]);
        return new GeoShape(null, lines, null, normalized, getBounds());
    }

    @Override
    public void addTo(Network network, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        network.addAllLinks(vects);
    }
    
    public List<LineString> splitOnSelfIntersect(Tolerance accuracy){
        return splitOnIntersect(getLineIndex(), accuracy);
    }
    
    /**
     * Split this line string on intersections with the lines given
     * @param against
     * @param accuracy
     * @return a list of lines
     */
    public List<LineString> splitOnIntersect(SpatialNode<Line> against, Tolerance accuracy){
        Network network = new Network();
        network.addAllLinks(vects);
        ArrayList<VectList> lineStrings = new ArrayList<>();
        network.extractLines(lineStrings, true);
        ArrayList<LineString> ret = new ArrayList<>();
        if(lineStrings.size() == 1){
            ret.add(this);
        }else{
            for(VectList lineString : lineStrings){
                ret.add(new LineString(lineString));
            }
        }
        return ret;
    }
    
    static void removeDuplicates(VectList vects){
        if(vects.isEmpty()){
            return;
        }
        int index = vects.size() - 1;
        double bx = vects.getX(index);
        double by = vects.getY(index);
        while (index-- > 0) {
            double ax = vects.getX(index);
            double ay = vects.getY(index);
            if ((ax == bx) && (ay == by)) {
                vects.remove(index+1);
            }
            bx = ax;
            by = ay;
        }
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

    public boolean isNormalized(Tolerance accuracy) {
        if (vects.size() < 2) {
            return false;
        }
        
        VectList points = vects.clone();
        points.sort();
        for(int i = 1; i < points.size(); i++){
            double bx = vects.getX(i);
            double by = vects.getY(i);
            for(int j = i; j-- > 0;){
                double ax = vects.getX(j);
                double ay = vects.getY(j);
                if (accuracy.match(ax, ay, bx, by)) {
                    return false;
                }else if(!accuracy.match(ax, bx)){
                    break;
                }
                bx = ax;
                by = ay;
            }
        }

        return true;
    }
    
    public boolean isNormalized(){
        Boolean ret = normalized;
        if(ret == null){
            ret = isNormalized(Tolerance.ZERO);
            normalized = ret;
        }
        return ret;
    }

    /**
     * Remove any colinear vertices and make sure ordered by lowest first
     *
     * @return
     */
    public List<LineString> normalize() {
        List<LineString> ret = new ArrayList<>();
        if(isNormalized()){
            ret.add(this);
        }else{
            splitOnIntersect(getLineIndex(), Tolerance.ZERO);
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

    public static LineString read(DataInput in) throws NullPointerException, IllegalArgumentException, GeomException {
        VectList vects = VectList.read(in);
        return valueOf(vects);
    }

    public void write(DataOutput out) throws NullPointerException, GeomException {
        vects.write(out);
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
        return Area.valueOf(network, accuracy);
    }
    
    static void removeNearLines(Network network, VectList lines, double threshold){
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
    
    Relate relateInternal(double x, double y, Tolerance accuracy){
        RelateProcessor processor = new RelateProcessor(x, y, accuracy);
        return getLineIndex().forInteracting(Rect.valueOf(x, y, x, y), processor) ? Relate.OUTSIDE : Relate.TOUCH;
    }

    @Override
    public Geom union(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        return union(other.toGeoShape(flatness, accuracy), accuracy);
        
    }
    
    public Geom union(GeoShape other, Tolerance accuracy) throws NullPointerException{
        if(other.isEmpty()){
            return this;
        }else if(other.getBounds().isDisjoint(getBounds(), accuracy)){ // quick way - disjoint
            LineString[] lines = new LineString[other.lines.length+1];
            lines[0] = this;
            System.arraycopy(other.lines, 0, lines, 1, other.lines.length);
            Arrays.sort(lines, COMPARATOR);
            return new GeoShape(other.area, lines, other.points);
        }        
        Network network = new Network();
        other.addLinesTo(network);
        network.addAllLinks(vects);
        network.explicitIntersections(accuracy);
        Area area = other.getArea();
        if(area != null){
            other.area.removeWithRelation(network, accuracy, Relate.INSIDE);
        }
        List<LineString> lineStrings = network.extractLineStrings();
        if((!other.hasNonLines()) && (lineStrings.size() == 1)){ // only a line returned
            return lineStrings.get(0);
        }
        LineString[] lines = lineStrings.toArray(new LineString[lineStrings.size()]);
        VectList points = other.points;
        if(points != null){
            VectList newPoints = new VectList();
            for(int p = 0; p < points.size(); p++){
                double x = points.getX(p);
                double y = points.getY(p);
                if(relateInternal(x, y, accuracy) == Relate.OUTSIDE){
                    newPoints.add(x, y);
                }
            }
            points = newPoints.isEmpty() ? null : newPoints;
        }
        return new GeoShape(other.area, lines, points);
    }

    @Override
    public Geom intersection(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        if(getBounds().isDisjoint(other.getBounds(), accuracy)){
            return null;
        }
        return intersection(other.toGeoShape(flatness, accuracy), flatness, accuracy);
    }

    public Geom intersection(GeoShape other, Tolerance accuracy) throws NullPointerException{
        if(other.isEmpty()){
            return null;
        }else if(other.getBounds().isDisjoint(getBounds(), accuracy)){ // quick way - disjoint
            return null;
        }
        Network network = new Network();
        network.addAllLinks(vects);
        other.addTo(network);
        network.explicitIntersections(accuracy);
        removeWithRelation(network, accuracy, Relate.OUTSIDE);
        other.removeWithRelation(network, accuracy, Relate.OUTSIDE);
        List<LineString> lineStrings = network.extractLineStrings();
        if((!other.hasNonLines()) && (lineStrings.size() == 1)){ // only a line returned
            return lineStrings.get(0);
        }
        LineString[] lines = lineStrings.toArray(new LineString[lineStrings.size()]);
        return new GeoShape(null, lines, null);
    }
    
    @Override
    public Geom less(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        if(getBounds().isDisjoint(other.getBounds(), accuracy)){
            return this;
        }
        return less(other.toGeoShape(flatness, accuracy), flatness, accuracy);
    }

    public Geom less(GeoShape other, Tolerance accuracy) throws NullPointerException{
        if(other.isEmpty()){
            return this;
        }else if(other.getBounds().isDisjoint(getBounds(), accuracy)){ // quick way - disjoint
            return this;
        }
        Network network = new Network();
        network.addAllLinks(vects);
        other.addTo(network);
        network.explicitIntersections(accuracy);
        removeWithRelation(network, accuracy, Relate.OUTSIDE);
        other.removeWithRelation(network, accuracy, Relate.INSIDE);
        List<LineString> lineStrings = network.extractLineStrings();
        if((!other.hasNonLines()) && (lineStrings.size() == 1)){ // only a line returned
            return lineStrings.get(0);
        }
        if(lineStrings.isEmpty()){
            return null;
        }
        LineString[] lines = lineStrings.toArray(new LineString[lineStrings.size()]);
        return new GeoShape(null, lines, null);
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

    private void removeWithRelation(final Network network, final Tolerance accuracy, final Relate relate) {
        network.map.forEach(new VectMapProcessor<VectList>(){
            @Override
            public boolean process(double x, double y, VectList value) {
                if(LineString.this.relateInternal(x, y, accuracy) == relate){
                    network.removeVertexInternal(x, y);
                }
                return true;
            }
        });
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
