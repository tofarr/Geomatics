package org.jg.geom;

import java.awt.geom.PathIterator;
import java.beans.Transient;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.jg.geom.LineString.NearLinkRemover;
import org.jg.geom.Network.VertexProcessor;
import org.jg.util.SpatialNode;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.VectList;
import org.jg.util.VectMap;
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
     * @throws IllegalArgumentException if the list was less than 4 vectors
     * long, or was unclosed
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
    static List<Ring> valueOf(Network network, Tolerance accuracy) throws NullPointerException {
        network = network.clone();
        network.explicitIntersections(accuracy);
        return valueOfInternal(network, accuracy);
    }
    
    static List<Ring> valueOfInternal(Network network, Tolerance accuracy) throws NullPointerException {
        
        ArrayList<Ring> ret = new ArrayList<>();
        int numVects = network.numVects();
        if (numVects == 0) {
            return ret; //no vectors so no rings
        }

        //get vectors in correct order - it is important that they are processed left to right
        VectList allVects = network.getVects(new VectList(numVects)); 

        // First we need to make sure there are no hang lines (lines ending in a vector linked to only one other vector)
        // as the algorithm can't process these
        VectList path = new VectList();
        VectBuilder vect = new VectBuilder();
        for (int v = allVects.size(); v-- > 0;) {
            removeHangLines(network, allVects.getX(v), allVects.getY(v));
        }

        //Process network
        VectBuilder workingVect = new VectBuilder();
        VectMap<Integer> vects = new VectMap<>();
        
        // For each link in ascending order
        for (int a = 0; a < allVects.size(); a++) { 
            double ax = allVects.getX(a);
            double ay = allVects.getY(a);
            VectList links = network.map.get(ax, ay);
            for (int b = 0; b < links.size(); b++) {
                double bx = links.getX(b);
                double by = links.getY(b);
                    
                followRing(ax, ay, bx, by, network, path, workingVect);

                //split path on points of self intersection
                vects.clear();
                for(int p = 0; p < path.size(); p++){
                    double x = path.getX(p);
                    double y = path.getY(p);
                    Integer index = vects.get(x, y);
                    if(index == null){
                        vects.put(x, y, p);
                    }else{
                        int numRingVects = p + 1 - index;
                        VectList ringPath = new VectList(numRingVects);
                        ringPath.addAll(path, index, numRingVects);
                        network.removeAllLinks(ringPath); // remove these links from the network - effectively marking them as processed
                        
                        //Any hanglines produced by removing the ring from the network need to be dealt with
                        for(int n = numRingVects - 1; n-- > 0;){
                            double nx = ringPath.getX(n);
                            double ny = ringPath.getY(n);
                            removeHangLines(network, nx, ny);
                        }
                        
                        ret.add(new Ring(ringPath, null, null, null, null, Boolean.TRUE, null, Boolean.TRUE));
                    }
                }
            }
        }
        return ret;
    }
    
    static void removeHangLines(Network network, double nx, double ny){
        VectList _links = network.map.get(nx, ny);
        while(_links.size() == 1){
            double mx = _links.getX(0);
            double my = _links.getY(0);
            network.removeLinkInternal(nx, ny, mx, my);
        }
    }
    
    
    //follow a ring around the edge of a network
    static void followRing(double ax, double ay, double bx, double by, Network network, VectList path, VectBuilder vect) {
        double ox = ax;
        double oy = ay;
        path.clear().add(ax, ay).add(bx, by);
        while (true) {
            network.nextCCW(bx, by, ax, ay, vect);
            path.add(vect);
            if(Vect.compare(ox, oy, vect.getX(), vect.getY()) == 0){
                return;
            }
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
     * Get the area of the vect list given, assuming that it is a valid closed
     * linear ring.
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

    public int numVects() {
        return vects.size();
    }

    public int numLines() {
        return vects.size() - 1;
    }

    /**
     * Get the bounds of this ring
     *
     * @return
     */
    @Transient
    @Override
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
     * @param accuracy tolerance for touch
     * @return relation
     * @throws NullPointerException if vect or tolerance was null
     */
    @Override
    public Relate relate(Vect vect, Tolerance accuracy) throws NullPointerException {
        return relateInternal(vect.x, vect.y, accuracy);
    }

    @Override
    public Relate relate(VectBuilder vect, Tolerance accuracy) throws NullPointerException {
        return relateInternal(vect.getX(), vect.getY(), accuracy);
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

    static Relate relateInternal(double x, double y, SpatialNode<Line> lineIndex, Tolerance tolerance) throws NullPointerException {
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
        if (getArea() < 0) {
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
     * Determine if the linear ring represented by the vectList given contains
     * only convex angles. Results are undefined if the edges are unclosed or
     * self intersect
     *
     * @param vects
     * @return
     * @throws IndexOutOfBoundsException if the vect list does not have at least
     * 4 elements
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

    public void addTo(Network network) {
        network.addAllLinks(vects);
    }

    @Override
    public Geom buffer(double amt, Tolerance flatness, Tolerance accuracy) {
        if (amt == 0) {
            return this;
        }
        VectList buffer = getEdgeBuffer(amt, flatness, accuracy);
        Network network = new Network();
        network.addAllLinks(buffer);
        network.explicitIntersections(accuracy);
        LineString.removeNearLines(network, vects, amt);
        return Area.valueOfInternal(network, accuracy);
    }

    public VectList getEdgeBuffer(double amt, Tolerance flatness, Tolerance tolerance) {
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

        double ax = vects.getX(vects.size() - 2);
        double ay = vects.getY(vects.size() - 2);
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

    @Override
    public Ring transform(Transform transform) {
        VectList transformed = vects.clone();
        transformed.transform(transform);
        Ring ring = new Ring(transformed);
        return ring;
    }

    @Override
    public PathIterator pathIterator() {
        return new PathIterator(){
            
            final int max = vects.size()-1;
            int index;
                    
            @Override
            public int getWindingRule() {
                return WIND_NON_ZERO;
            }

            @Override
            public boolean isDone() {
                return index > max;
            }

            @Override
            public void next() {
                index++;
            }

            @Override
            public int currentSegment(float[] coords) {
                coords[0] = (float)vects.getX(index);
                coords[0] = (float)vects.getY(index);
                if(index == 0){
                    return SEG_MOVETO;
                }else if(index == max){
                    return SEG_CLOSE;
                }else{
                    return SEG_LINETO;
                }
            }

            @Override
            public int currentSegment(double[] coords) {
                coords[0] = vects.getX(index);
                coords[0] = vects.getY(index);
                if(index == 0){
                    return SEG_MOVETO;
                }else if(index == max){
                    return SEG_CLOSE;
                }else{
                    return SEG_LINETO;
                }
            }
                
        };
    }

    @Override
    public Ring clone() {
        return this;
    }

    @Override
    public GeoShape toGeoShape(Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        return toGeoShape();
    }

    public GeoShape toGeoShape() {
        return new GeoShape(toArea(), GeoShape.NO_LINES, null);
    }

    @Override
    public void addTo(Network network, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        network.addAllLinks(vects);
    }

    public Area toArea() throws NullPointerException {
        return new Area(this);
    }

    @Override
    public Geom union(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        return toArea().union(other, flatness, accuracy);
    }
    
    @Override
    public Geom intersection(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        if(getBounds().isDisjoint(other.getBounds(), accuracy)){
            return null;
        }
        return toArea().intersection(other, flatness, accuracy);
    }

    @Override
    public Geom less(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        if(getBounds().isDisjoint(other.getBounds(), accuracy)){
            return this;
        }
        return toArea().less(other, flatness, accuracy);
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
     * @throws IllegalArgumentException if the stream contained infinite or NaN
     * ordinates
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
