package org.jg.geom;

import java.awt.geom.PathIterator;
import java.beans.Transient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jg.util.SpatialNode;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.VectList;
import org.jg.util.VectMap;
import org.jg.util.VectMap.VectMapProcessor;

/**
 * Immutable 2D Linear Ring. Checks are in place to insure that:
 * <ul>
 * <li>Ordinates are not infinite or NaN</li>
 * <li>The ring is closed (First and last point are the same)</li>
 * <li>The ring has at least 4 coordinates (The first being the same as the last point)</li>
 * <li>Duplicate coordinates are not present (Aside from the first and last point)</li>
 * <li>That the ring does not self intersect</li>
 * <li>Points of the ring are in anti clockwise order</li>
 * </ul>
 * 
 * @author tofar_000
 */
public class Ring implements Geom {

    static final Ring[] EMPTY = new Ring[0];
    final VectList vects;
    private SpatialNode<Line> lineIndex;
    private Double area;
    private Double length;
    private Vect centroid;
    private Boolean convex;

    Ring(VectList vects, SpatialNode<Line> lineIndex, Double area, Double length, Vect centroid, Boolean convex) {
        this.vects = vects;
        this.lineIndex = lineIndex;
        this.area = area;
        this.length = length;
        this.centroid = centroid;
        this.convex = convex;
    }

    Ring(VectList vects, Double area) {
        this.vects = vects;
        this.area = area;
    }

    /**
     * Extract any available rings from the network given
     *
     * @param accuracy
     * @param ords
     * @return list of rings
     * @throws NullPointerException if ords or accuracy was null
     * @throws IllegalArgumentException if the vectors did not form a single linear ring (unclosed
     * or self intersecting)
     */
    public static Ring valueOf(Tolerance accuracy, double... ords) throws NullPointerException, IllegalArgumentException {
        return valueOf(accuracy, new VectList(ords));
    }

    /**
     * Extract any available rings from the network given
     *
     * @param accuracy
     * @param vects
     * @return list of rings
     * @throws NullPointerException if vects or accuracy was null
     * @throws IllegalArgumentException if the vectors did not form a single linear ring (unclosed
     * or self intersecting)
     */
    public static Ring valueOf(Tolerance accuracy, VectList vects) throws NullPointerException, IllegalArgumentException {
        Network network = new Network();
        network.addAllLinks(vects);
        network.explicitIntersections(accuracy);
        network.snap(accuracy);
        List<Ring> rings = parseAllInternal(network, accuracy);
        if (rings.size() != 1) {
            throw new IllegalArgumentException("Vectors did not form a single linear ring : " + vects);
        }
        return rings.get(0);
    }

    /**
     * Extract any available rings from the network given
     *
     * @param accuracy
     * @param network
     * @return list of rings
     * @throws NullPointerException if network was null
     */
    public static Ring[] parseAll(Tolerance accuracy, Network network) throws NullPointerException {
        network = network.clone();
        network.explicitIntersections(accuracy);
        List<Ring> rings = parseAllInternal(network, accuracy);
        return rings.isEmpty() ? EMPTY : rings.toArray(new Ring[rings.size()]);
    }

    static List<Ring> parseAllInternal(Network network, Tolerance accuracy) throws NullPointerException {

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
            if (links == null) {
                continue;
            }
            for (int b = 0; b < links.size(); b++) {
                double bx = links.getX(b);
                double by = links.getY(b);

                followRing(ax, ay, bx, by, network, path, workingVect);

                //split path on points of self intersection
                vects.clear();
                for (int p = 0; p < path.size(); p++) {
                    double x = path.getX(p);
                    double y = path.getY(p);
                    Integer index = vects.get(x, y);
                    if (index == null) {
                        vects.put(x, y, p);
                    }else{
                        int numRingVects = p + 1 - index;
                        VectList ringPath = new VectList(numRingVects);
                        ringPath.addAll(path, index, numRingVects);
                        network.removeAllLinks(ringPath); // remove these links from the network - effectively marking them as processed
                        numRingVects--;
                        path.removeAll(index, numRingVects);
                        p -= numRingVects;
                        
                        //Any hanglines produced by removing the ring from the network need to be dealt with
                        for (int n = numRingVects; n-- > 0;) {
                            double nx = ringPath.getX(n);
                            double ny = ringPath.getY(n);
                            removeHangLines(network, nx, ny);
                        }
                        
                        int minIndex = minIndex(ringPath);
                        if(minIndex != 0){
                            ringPath = rotate(ringPath, minIndex);
                        }

                        ret.add(new Ring(ringPath, null));
                    }
                }
            }
        }
        return ret;
    }

    static void removeHangLines(Network network, double nx, double ny) {
        VectList _links = network.map.get(nx, ny);
        if (_links == null) {
            return;
        }
        if (_links.size() == 0) {
            network.map.remove(nx, ny);
        }
        while (_links.size() == 1) {
            double mx = _links.getX(0);
            double my = _links.getY(0);
            network.removeVertexInternal(nx, ny);
            _links = network.map.get(mx, my);
            nx = mx;
            ny = my;
            //network.removeLinkInternal(nx, ny, mx, my);
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
            if (Vect.compare(ox, oy, vect.getX(), vect.getY()) == 0) {
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

    /**
     * Get the number of vectors
     *
     * @return
     */
    public int numVects() {
        return vects.size();
    }

    /**
     * Get the number of lines
     *
     * @return
     */
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

    /**
     * Get the centroid of this ring
     * @return
     */
    @Transient
    public Vect getCentroid() {
        Vect ret = centroid;
        if (ret == null) {
            ret = getCentroid(vects);
            centroid = ret;
        }
        return ret;
    }

    /**
     * Get the centroid of the vects given representing a non self intersecting closed ring
     *
     * @param vects
     * @return
     */
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
     * Determine if the linear ring represented by the vectList given contains only convex angles.
     * Results are undefined if the edges are unclosed or self intersect
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

    /**
     * Get the vector at the index given
     *
     * @param index
     * @return
     * @throws IndexOutOfBoundsException if index was out of bounds
     */
    public Vect getVect(int index) throws IndexOutOfBoundsException {
        return vects.getVect(index);
    }

    /**
     * Get the vector at the index given
     *
     * @param index
     * @param target target in which to place vector
     * @return target
     * @throws IndexOutOfBoundsException if index was out of bounds
     */
    public VectBuilder getVect(int index, VectBuilder target) throws IndexOutOfBoundsException {
        return vects.getVect(index, target);
    }

    /**
     * Get the line at the index given
     *
     * @param index
     * @return
     * @throws IndexOutOfBoundsException if index was out of bounds
     */
    public Line getLine(int index) throws IndexOutOfBoundsException {
        return vects.getLine(index);
    }

    /**
     * Get the x value at the index given
     *
     * @param index
     * @return
     * @throws IndexOutOfBoundsException if index was out of bounds
     */
    public double getX(int index) throws IndexOutOfBoundsException {
        return vects.getX(index);
    }

    /**
     * Get the y value at the index given
     *
     * @param index
     * @return
     * @throws IndexOutOfBoundsException if index was out of bounds
     */
    public double getY(int index) throws IndexOutOfBoundsException {
        return vects.getY(index);
    }

    /**
     * Get vectors from this ring and add them to the target list given
     *
     * @param target
     * @return
     */
    public VectList getVects(VectList target) {
        return target.addAll(vects);
    }

    /**
     * Add this ring to the network given
     *
     * @param network
     */
    public void addTo(Network network) {
        network.addAllLinks(vects);
    }

    @Override
    public Area buffer(double amt, Tolerance flatness, Tolerance accuracy) {
        if (amt == 0) {
            return new Area(this, Area.NO_CHILDREN);
        }
        VectList buffer = getEdgeBuffer(amt, flatness, accuracy);
        return buildAreaFromRing(buffer, accuracy);
    }

    static Area buildAreaFromRing(VectList closedRing, Tolerance accuracy){
        
        Area union = null;
        Area less = null;
        
        //Sanitize the ring using a network
        Network network = new Network();
        network.addAllLinks(closedRing);
        network.explicitIntersections(accuracy);
        network.snap(accuracy);
        closedRing = parsePathFromNetwork(network, closedRing, accuracy);
        //int minIndex = minIndex(closedRing);
        //if(minIndex != 0){
            //rotate(closedRing, minIndex);
        //}

        VectMap<Integer> indices = new VectMap<>();
        for(int i = 0; i < closedRing.size(); i++){
            double x = closedRing.getX(i);
            double y = closedRing.getY(i);
            final Integer index = indices.get(x, y);
            if(index != null){
                final int numVects = i - index;
                VectList ring = new VectList(numVects + 1);
                ring.addAll(closedRing, index, numVects + 1);
                closedRing.removeAll(index, i - index);
                final VectMap<Integer> newIndices = new VectMap<>();
                indices.forEach(new VectMapProcessor<Integer>() {
                    @Override
                    public boolean process(double x, double y, Integer value) {
                        if(value <= index){
                            newIndices.put(x, y, value);
                        }
                        return true;
                    }
                });
                indices = newIndices;
                i = index;
                int minIndx = minIndex(ring);
                if(minIndx != 0){
                    rotate(ring, minIndx);
                }
                double a = getArea(ring);
                if(a > 0){
                    Area area = new Ring(ring, a).toArea();
                    union = (union == null) ? area : union.union(area, accuracy);
                }else{
                    ring.reverse();
                    Area area = new Ring(ring, a).toArea();
                    less = (less == null) ? area : less.union(area, accuracy);
                }
            }
            VectList links = network.map.get(x, y);
            if((i == 0) || (links.size() != 2)){
                indices.put(x, y, i);
            }
        }
        Area ret = union;
        if((union != null) && (less != null)){
            ret = union.less(less, accuracy);
        }
        return ret;
    }
        
    static VectList parsePathFromNetwork(Network network, VectList template, Tolerance tolerance){
        VectList ret = new VectList();
        VectBuilder a = new VectBuilder();
        VectBuilder b = new VectBuilder();
        VectBuilder c = new VectBuilder();
        template.getVect(0, a);
        if(!network.hasVect(a.getX(), a.getY())){ // a was snapped out - get closest point on a
            b.set(a.getX() + tolerance.tolerance * 10, a.getY() + tolerance.tolerance * 10);
            snapToNearest(network.getLinks(), a, tolerance, b);
            VectBuilder tmp = a;
            a = b;
            b = tmp;
        }
        ret.add(a);
        VectList links = new VectList();
        double tolSq = tolerance.tolerance * tolerance.tolerance;
        for(int i = 1; i < template.size(); i++){
            template.getVect(i, b);
            if(!network.hasVect(b.getX(), b.getY())){ // a was snapped out - get closest point on a
                c.set(b.getX() + tolerance.tolerance * 10, b.getY() + tolerance.tolerance * 10);
                snapToNearest(network.getLinks(), b, tolerance, c);
                VectBuilder tmp = b;
                b = c;
                c = tmp;
            }
            while(!network.hasLink(a.getX(), a.getY(), b.getX(), b.getY())){
                network.getLinks(a.getX(), a.getY(), links);
                double distA = Vect.distSq(a.getX(), a.getY(), b.getX(), b.getY());
                int j = links.size();
                while(true){
                    j--;
                    links.getVect(j, c);
                    //IF C is ON LINE AB, and C is closer to B than A, add C to ret and set C to A and break
                    if(Line.vectLineDistSq(a.getX(), a.getY(), b.getX(), b.getY(), c.getX(), c.getY()) <= tolSq){
                        double distC = Vect.distSq(c.getX(), c.getY(), b.getX(), b.getY());
                        if(distC < distA){
                            ret.add(c);
                            VectBuilder tmp = c;
                            c = a;
                            a = tmp;
                            break;
                        }
                    }
                }
            }
            ret.add(b);
            VectBuilder tmp = a;
            a = b;
            b = tmp;
        }
        return ret;
    }
    
    static void snapToNearest(SpatialNode<Line> node, VectBuilder vect, Tolerance tolerance, VectBuilder result){
        double x = vect.getX();
        double y = vect.getY();
        if(node.isDisjoint(x, y, x, y, tolerance)){
            return;
        }
        if(node.isBranch()){
            snapToNearest(node.getA(), vect, tolerance, result);
            snapToNearest(node.getB(), vect, tolerance, result);
            return;
        }
        double dist = Vect.distSq(x, y, result.getX(), result.getY());
        for(int i = node.size(); i-- > 0;){
            Line line = node.getItemValue(i);
            double distA = Vect.distSq(x, y, line.ax, line.ay);
            if(distA < dist){
                line.getA(result);
                dist = distA;
            }
            double distB = Vect.distSq(x, y, line.bx, line.by);
            if(distB < dist){
                line.getB(result);
                dist = distB;
            }
        }
    }
    
    /**
     * Get an edge buffer from this ring
     *
     * @param amt
     * @param flatness
     * @param tolerance
     * @return a VectList representing an edge buffer
     */
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
        if (transform.mode == Transform.NO_OP) {
            return this;
        }
        VectList transformed = vects.clone();
        transformed.transform(transform);
        int minIndex = minIndex(transformed);
        if(minIndex != 0){
            transformed = rotate(transformed, minIndex);
        }
        double area = getArea(transformed);
        if(area < 0){
            transformed.reverse();
            area = -area;
        }
        Ring ring = new Ring(transformed, area);
        return ring;
    }

    @Override
    public PathIterator pathIterator() {
        return new PathIterator() {

            final int max = vects.size() - 1;
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
                coords[0] = (float) vects.getX(index);
                coords[1] = (float) vects.getY(index);
                if (index == 0) {
                    return SEG_MOVETO;
                } else if (index == max) {
                    return SEG_CLOSE;
                } else {
                    return SEG_LINETO;
                }
            }

            @Override
            public int currentSegment(double[] coords) {
                coords[0] = vects.getX(index);
                coords[1] = vects.getY(index);
                if (index == 0) {
                    return SEG_MOVETO;
                } else if (index == max) {
                    return SEG_CLOSE;
                } else {
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

    /**
     * Convert this ring to a GeoShape
     *
     * @return
     */
    public GeoShape toGeoShape() {
        return new GeoShape(toArea(), null, null);
    }

    @Override
    public void addTo(Network network, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        network.addAllLinks(vects);
    }

    /**
     * Create a new Area based on this ring
     *
     * @return
     */
    public Area toArea() {
        return new Area(this);
    }

    @Override
    public Geom union(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        return toArea().union(other, flatness, accuracy);
    }

    @Override
    public Geom intersection(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        if (getBounds().isDisjoint(other.getBounds(), accuracy)) {
            return null;
        }
        return toArea().intersection(other, flatness, accuracy);
    }

    @Override
    public Geom less(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        if (getBounds().isDisjoint(other.getBounds(), accuracy)) {
            return this;
        }
        Area area = toArea().less(other, flatness, accuracy);
        return (area == null) ? null : area.simplify();
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
        StringBuilder str = new StringBuilder();
        toString(str);
        return str.toString();
    }

    /**
     * Convert this ring to astirng and place it in the appendable given
     *
     * @param appendable
     * @throws GeomException if there was an IO Error
     * @throws NullPointerException if appendable was null
     */
    @Override
    public void toString(Appendable appendable) throws GeomException, NullPointerException {
        try {
            appendable.append("[\"RG\"");
            for (int i = 0; i < vects.size(); i++) {
                appendable.append(", ").append(Vect.ordToStr(vects.getX(i))).append(',').append(Vect.ordToStr(vects.getY(i)));
            }
            appendable.append(']');
        } catch (IOException ex) {
            throw new GeomException("Error writing LineStirng", ex);
        }
    }
}
