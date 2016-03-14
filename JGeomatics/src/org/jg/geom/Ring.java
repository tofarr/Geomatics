package org.jg.geom;

import java.beans.Transient;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.jg.geom.Network.VertexProcessor;
import org.jg.util.SpatialNode;
import org.jg.util.SpatialNode.NodeProcessor;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.VectList;
import org.jg.util.VectSet;

/**
 * Linear Ring
 *
 * @author tofar_000
 */
public class Ring implements Serializable, Cloneable, Geom {

    final VectList vects;
    private SpatialNode<Line> lineIndex;
    private Double area;
    private Double length;
    private Vect centroid;
    private Boolean valid;
    private Boolean convex;
    private Boolean normalized;

    Ring(VectList vects, SpatialNode<Line> lineIndex, Double area, Double length, Vect centroid, Boolean valid, Boolean convex, Boolean normalized) {
        this.vects = vects;
        this.lineIndex = lineIndex;
        this.area = area;
        this.length = length;
        this.centroid = centroid;
        this.valid = valid;
        this.convex = convex;
        this.normalized = normalized;
    }

    /**
     * Create a ring based on the list of vectors given
     *
     * @param vects
     * @throws NullPointerException if vects was null
     * @throws IllegalArgumentException if the list was less than 4 vectors long, or was unclosed
     */
    public Ring(VectList vects) throws NullPointerException, IllegalArgumentException {
        int last = vects.size() - 1;
        if (last < 3) {
            throw new IllegalArgumentException("Rings must have at least 4 points");
        }
        if ((vects.getX(0) != vects.getX(last)) || (vects.getY(0) != vects.getY(last))) {
            throw new IllegalArgumentException("Rings must be closed!");
        }
        this.vects = vects.clone();
    }

    private Ring(VectList vects, Double area) {
        this.vects = vects;
        this.area = area;
    }

    /**
     * Extract any available rings from the network given
     *
     * @param network
     * @return list of rings
     * @throws NullPointerException if network was null
     */
    public static List<Ring> valueOf(Network network) throws NullPointerException {

        ArrayList<Ring> ret = new ArrayList<>();
        int numVects = network.numVects();
        if (numVects == 0) {
            return ret; //no vectors so no rings
        }

        VectList allVects = network.getVects(new VectList(numVects)); //get vectors in correct order

        //First we need to make sure there are no hang lines
        boolean cloned = false;
        VectList path = new VectList();
        VectBuilder vect = new VectBuilder();
        for (int v = allVects.size(); v-- > 0;) {
            double ax = allVects.getX(v);
            double ay = allVects.getY(v);
            int numLinks = network.numLinks(ax, ay);
            if (numLinks <= 1) {
                if (!cloned) {
                    network = network.clone();
                    cloned = true;
                }
                if (numLinks == 0) {
                    network.removeVertex(ax, ay);
                }
                while (numLinks == 1) {
                    network.getLink(ax, ay, 0, vect);
                    network.removeVertex(ax, ay);
                    ax = vect.getX();
                    ay = vect.getY();
                    numLinks = network.numLinks(ax, ay);
                }
            }
        }

        //traverse identifying links
        final Network visited = new Network(); // network storing visited links
        VectList links = new VectList();
        VectSet pathSet = new VectSet();
        for (int a = 0; a < allVects.size(); a++) {
            double ax = allVects.getX(a);
            double ay = allVects.getY(a);
            links.clear();
            network.getLinks(ax, ay, links);
            for (int b = 0; b < links.size(); b++) {
                double bx = links.getX(b);
                double by = links.getY(b);
                if (!visited.hasLink(ax, ay, bx, by)) {
                    //traverse - if self intersects, then invalid. If area is negative at end then invalid 
                    followRing(ax, ay, bx, by, network, path, pathSet, vect);
                    int s = path.size();
                    //if (s <= 3) {
                    //    continue; // not enough points for a ring - traversal was invalid - impossible since hang lines have been filtered
                    //} else
                    if ((path.getX(s - 1) != ax) || (path.getY(s - 1) != ay)) {
                        continue; // not a ring - traversal was invalid
                    }
                    double area = Ring.getArea(path);
                    if (area <= 0) { // not a valid  ring
                        continue;
                    }
                    int index = minIndex(path);
                    VectList ringPath = (index == 0) ? path.clone() : rotate(path, index);
                    Ring ring = new Ring(ringPath, area);
                    ret.add(ring);
                    visited.addAllLinks(path);
                }
            }
        }
        return ret;
    }

    //follow a ring around the edge of a network
    private static void followRing(double ax, double ay, double bx, double by, Network network, VectList path, VectSet pathSet, VectBuilder vect) {
        path.clear().add(ax, ay).add(bx, by);
        pathSet.clear().add(ax, ay).add(bx, by);
        while (true) {
            network.nextCW(bx, by, ax, ay, vect);
            path.add(vect);
            if (pathSet.contains(vect)) {
                return;
            }
            pathSet.add(vect);
            ax = bx;
            ay = by;
            bx = vect.getX();
            by = vect.getY();
        }
    }

    /**
     * Get the area of this ring
     *
     * @return
     */
    @Transient
    public double getArea() {
        Double ret = area;
        if (ret == null) {
            ret = getArea(vects);
            area = ret;
        }
        return ret;
    }

    /**
     * Get the area of the vect list given, assuming that it is a valid closed linear ring.
     *
     * @param vects
     * @return area
     * @throws NullPointerException if vects was null
     * @throws IndexOutOfBoundsException if vects does not have enough elements
     */
    public static double getArea(VectList vects) throws NullPointerException, IndexOutOfBoundsException {
        int index = vects.size();
        double bx = vects.getX(--index);
        double by = vects.getY(index);
        double area = 0;
        while (index > 0) {
            double ax = vects.getX(--index);
            double ay = vects.getY(index);
            area += (ax * by) - (bx * ay);
            bx = ax;
            by = ay;
        }
        area /= 2;
        return area;
    }

    /**
     * Get the length of the perimeter of this ring
     *
     * @return the length of the perimeter of this ring
     */
    @Transient
    public double getLength() {
        Double ret = length;
        if (ret == null) {
            ret = LineString.getLength(vects);
            length = ret;
        }
        return ret;
    }
    
    public int numVects(){
        return vects.size();
    }
    
    public int numLines(){
        return vects.size() - 1;
    }

    /**
     * Get the bounds of this ring
     *
     * @return
     */
    @Transient
    public Rect getBounds() {
        return vects.getBounds();
    }

    /**
     * Add the bounds of this ring to the builder given
     *
     * @param bounds
     * @throws NullPointerException if bounds was null
     */
    public void addBoundsTo(RectBuilder bounds) throws NullPointerException {
        bounds.add(vects.getBounds());
    }

    /**
     * Determine the relation between the point given and this ring
     *
     * @param vect
     * @param tolerance tolerance for touch
     * @return relation
     * @throws NullPointerException if vect or tolerance was null
     */
    public Relate relate(Vect vect, Tolerance tolerance) throws NullPointerException {
        return relateInternal(vect.x, vect.y, tolerance);
    }

    /**
     * Determine the relation between the point given and this ring
     *
     * @param x
     * @param y
     * @param tolerance tolerance for touch
     * @return relation
     * @throws IllegalArgumentException if x or y was infinite or NaN
     * @throws NullPointerException if tolerance was null
     */
    public Relate relate(double x, double y, Tolerance tolerance) throws IllegalArgumentException, NullPointerException {
        Vect.check(x, y);
        return relateInternal(x, y, tolerance);
    }

    Relate relateInternal(double x, double y, Tolerance tolerance) throws NullPointerException {
        return relateInternal(x, y, getLineIndex(), tolerance);
    }
    
    static Relate relateInternal(double x, double y, SpatialNode<Line> lineIndex, Tolerance tolerance) throws NullPointerException{
        Rect bounds = lineIndex.getBounds();
        if (bounds.relateInternal(x, y, tolerance) == Relate.OUTSIDE) { // If outside bounds, then cant be inside
            return Relate.OUTSIDE;
        }
        Rect selection = Rect.valueOf(x, y, bounds.maxX, y);
        RelationProcessor processor = new RelationProcessor(tolerance, x, y);
        lineIndex.forInteracting(selection, processor);
        return processor.getRelate();
    }

    /**
     * Get an index of all lines in this ring
     *
     * @return
     */
    @Transient
    public SpatialNode<Line> getLineIndex() {
        SpatialNode<Line> ret = lineIndex;
        if (ret == null) {
            ret = vects.toLineIndex().getRoot();
            lineIndex = ret;
        }
        return ret;
    }

    @Transient
    public boolean isValid() {
        Boolean ret = valid;
        if (ret == null) {

            Network network = new Network();
            network.addAllLinks(vects);
            network.explicitIntersections(Tolerance.ZERO);
            ret = network.forEachVertex(new VertexProcessor() {
                @Override
                public boolean process(double x, double y, int numLinks) {
                    return numLinks == 2;
                }
            });

            if (ret) {
                ret = (getArea() > 0);
            }
            valid = ret;
        }
        return ret;
    }

    public Ring normalize() {
        int index = minIndex(vects);
        if (index == 0) {
            return this;
        }
        VectList rotated = rotate(vects, index);
        if(getArea() < 0){
            rotated.reverse();
        }
        Ring ret = new Ring(rotated, Math.abs(area));
        ret.centroid = centroid;
        ret.length = length;
        return ret;
    }

    static int minIndex(VectList ring) throws IndexOutOfBoundsException {
        int ret = 0;
        double x = ring.getX(0);
        double y = ring.getY(0);
        for (int i = ring.size() - 1; i-- > 0;) {
            double bx = ring.getX(i);
            double by = ring.getY(i);
            if (Vect.compare(x, y, bx, by) > 0) {
                ret = i;
                x = bx;
                y = by;
            }
        }
        return ret;
    }

    static VectList rotate(VectList ring, int index) throws IndexOutOfBoundsException {
        VectList ret = new VectList(ring.size());
        for (int i = 0, s = ring.size() - 1; i < s; i++) {
            ret.add(ring, index);
            index++;
            if (index == s) {
                index = 0;
            }
        }
        ret.add(ret, 0);
        return ret;
    }

    @Transient
    public Vect getCentroid() {
        Vect ret = centroid;
        if (ret == null) {
            ret = getCentroid(vects);
            centroid = ret;
        }
        return ret;
    }

    public static Vect getCentroid(VectList vects) {
        int index = vects.size() - 1;
        double bx = vects.getX(index);
        double by = vects.getY(index);
        double twiceArea = 0;
        double x = 0;
        double y = 0;
        while (index-- > 0) {
            double ax = vects.getX(index);
            double ay = vects.getY(index);
            double f = (bx * ay) - (ax * by);
            twiceArea += f;
            x += (ax + bx) * f;
            y += (ay + by) * f;
            bx = ax;
            by = ay;
        }
        double f = twiceArea * 3;
        return Vect.valueOf(x / f, y / f);
    }

    /**
     * Determine if the ring is convex.
     *
     * @return true if the ring was convex, false otherwise
     */
    @Transient
    public boolean isConvex() {
        Boolean ret = convex;
        if (ret == null) {
            ret = isConvex(vects);
            convex = ret;
        }
        return ret;
    }

    /**
     * Determine if the linear ring represented by the vectList given contains only convex angles. Results are undefined if the edges are
     * unclosed or self intersect
     *
     * @param vects
     * @return
     * @throws IndexOutOfBoundsException if the vect list does not have at least 4 elements
     */
    public static boolean isConvex(VectList vects) throws IndexOutOfBoundsException {
        int s = vects.size();
        double ax = vects.getX(s - 2);
        double ay = vects.getY(s - 2);
        double bx = vects.getX(0);
        double by = vects.getY(0);
        for (int i = 1; i < vects.size(); i++) {
            double cx = vects.getX(i);
            double cy = vects.getY(i);
            if (Line.counterClockwise(ax, ay, cx, cy, bx, by) == -1) {
                return false;
            }
            ax = bx;
            ay = by;
            bx = cx;
            by = cy;
        }
        return true;
    }

    public VectList getVects(VectList target) {
        return target.addAll(vects);
    }

    public void addTo(Network network, Tolerance tolerance) {
        network.addAllLinks(vects);
    }
    
    public Area buffer(double amt, Tolerance flatness, Tolerance tolerance){
        VectList edgeBuffer = getEdgeBuffer(amt, flatness, tolerance);
        Network network = new Network();
        network.addAllLinks(edgeBuffer);
        LineString.removeWithinBuffer(vects, network, amt, flatness, tolerance);
        return Area.valueOf(network);
    }
    
    public VectList getEdgeBuffer(double amt, Tolerance flatness, Tolerance tolerance){
        Vect.check(amt, "Invalid amt {0}");
        if (amt == 0) {
            return vects.clone();
        }
        return getEdgeBuffer(vects, amt, flatness, tolerance);
    }
    
    //The buffer produced by this may be self overlapping, and will need to be cleaned in a network before use
    static VectList getEdgeBuffer(VectList vects, double amt, Tolerance flatness, Tolerance tolerance) {
        VectList result = new VectList(vects.size() << 1);
        VectBuilder vect = new VectBuilder();

        double ax = vects.getX(vects.size()-2);
        double ay = vects.getY(vects.size()-2);
        double bx = vects.getX(0);
        double by = vects.getY(0);

        int index = 0;
        while (++index < vects.size()) {
            double cx = vects.getX(index);
            double cy = vects.getY(index);
            LineString.projectOutward(ax, ay, bx, by, cx, cy, amt, flatness, tolerance, vect, result);
            ax = bx;
            bx = cx;
            ay = by;
            by = cy;
        }

        Line.projectOutward(ax, ay, bx, by, 1, amt, tolerance, vect);
        result.add(vect);

        return result;
    }
    
    public Ring transform(Transform transform){
        VectList transformed = vects.clone();
        transformed.transform(transform);
        Ring ring = new Ring(transformed);
        return ring;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + vects.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Ring) && vects.equals(((Ring) obj).vects);
    }

    @Override
    public String toString() {
        return vects.toString();
    }

    /**
     *
     * @param appendable
     * @throws GeomException
     */
    public void toString(Appendable appendable) throws GeomException {
        vects.toString(appendable);
    }

    /**
     * Convert this ring to a string
     *
     * @param summarize true if shortened format may be used, false otherwise
     * @return string representation of this ring
     */
    public String toString(boolean summarize) {
        return vects.toString(summarize);
    }

    /**
     * Read a VectList from to the DataInput given
     *
     * @param in
     * @return a VectList
     * @throws NullPointerException if in was null
     * @throws IllegalArgumentException if the stream contained infinite or NaN ordinates
     * @throws GeomException if there was an IO error
     */
    public static Ring read(DataInput in) throws NullPointerException, IllegalArgumentException, GeomException {
        return new Ring(VectList.read(in));
    }

    /**
     * Write this Ring to the DataOutput given
     *
     * @param out
     * @throws NullPointerException if out was null
     * @throws GeomException if there was an IO error
     */
    public void write(DataOutput out) throws NullPointerException, GeomException {
        vects.write(out);
    }
}
