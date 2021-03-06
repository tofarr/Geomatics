package org.geomatics.geom;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.geomatics.algorithm.ConvexHull;
import org.geomatics.geom.io.GeomJaysonWriter;
import org.geomatics.geom.io.RingHandler;
import org.geomatics.util.SpatialNode;
import org.geomatics.util.Tolerance;
import org.geomatics.util.Transform;
import org.geomatics.util.VectList;
import org.geomatics.util.VectMap;
import org.geomatics.util.VectMap.VectMapProcessor;

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

    public static final String CODE = "RG";
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
        List<Ring> rings = parseAllInternal(network, accuracy, true);
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
        List<Ring> rings = parseAllInternal(network, accuracy, true);
        return rings.isEmpty() ? EMPTY : rings.toArray(new Ring[rings.size()]);
    }

    static List<Ring> parseAllInternal(final Network network, Tolerance accuracy, boolean removeHangLines) throws NullPointerException {
        
        ArrayList<Ring> ret = new ArrayList<>();
        
        //First remove all hang lines from the network - these cannot possibly be part of rings
        if(removeHangLines){
            network.removeHangLines();
        }
        if(network.numVects() < 2){
            return ret; // not enough links for a ring, so cannot be any rings!
        }
        
        Network processed = new Network(); // this stores all processed links

        //get vectors in correct order - it is important that they are processed left to right
        VectList allVects = network.getVects(new VectList(network.numVects()));
        VectList path = new VectList();
        VectBuilder workingVect = new VectBuilder();
        VectMap indices = new VectMap();
        for(int a = 0; (processed.numLinks() < network.numLinks()) && (a < allVects.size()); a++){
            double ax = allVects.getX(a);
            double ay = allVects.getY(a);
            VectList links = network.map.get(ax, ay);
            
            for (int b = 0; b < links.size(); b++) {
                double bx = links.getX(b);
                double by = links.getY(b);
                if(processed.hasLink(ax, ay, bx, by)){
                    continue; // link was already processed
                }

                processRings(ax, ay, bx, by, network, path, workingVect, indices, processed, ret);
            }
        }
        return ret;
    }
    
    static void processRings(double ax, double ay, double bx, double by, Network network, VectList path,
            VectBuilder workingVect, VectMap<Integer> indices, Network processed, Collection<Ring> results){
        path.clear().add(ax, ay).add(bx, by);
        indices.clear();
        indices.put(ax, ay, 0);
        indices.put(bx, by, 1);
        while (true) {
            network.nextCW(bx, by, ax, ay, workingVect);
            path.add(workingVect);
            Integer index = indices.get(workingVect);
            
            ax = bx;
            ay = by;
            bx = workingVect.getX();
            by = workingVect.getY();
            
            if(index == null){ // point is not already in path, add it and continue
                indices.put(workingVect.getX(), workingVect.getY(), path.size()-1);
                continue;
            }
            
            //we potentially have a ring, pull parts out of path to see
            int numVects = path.size() - index;
            VectList ringPath = new VectList(numVects);
            ringPath.addAll(path, index, numVects);
            path.removeAll(index+1, numVects-1);
            double area = getArea(ringPath);
            if(area > 0){
                int min = minIndex(ringPath);
                if(min != 0){
                    ringPath = rotate(ringPath, min);
                }
                processed.addAllLinks(ringPath);
                results.add(new Ring(ringPath, area));
            }

            if(index == 0){
                return; // all finished
            }
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
    
    @Override
    public double getArea(Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        return getArea();
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
    public int numPoints() {
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
    public int relate(Vect vect, Tolerance accuracy) throws NullPointerException {
        return relateInternal(vect.x, vect.y, accuracy);
    }

    @Override
    public int relate(VectBuilder vect, Tolerance accuracy) throws NullPointerException {
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
    public int relate(double x, double y, Tolerance tolerance) throws IllegalArgumentException, NullPointerException {
        Vect.check(x, y);
        return relateInternal(x, y, tolerance);
    }

    int relateInternal(double x, double y, Tolerance tolerance) throws NullPointerException {
        return relateInternal(x, y, getLineIndex(), tolerance);
    }

    static int relateInternal(double x, double y, SpatialNode<Line> lineIndex, Tolerance tolerance) throws NullPointerException {
        Rect bounds = lineIndex.getBounds();
        if (bounds.relateInternal(x, y, tolerance) == Relation.DISJOINT) { // If outside bounds, then cant be inside
            return Relation.DISJOINT;
        }
        Rect selection = Rect.valueOf(x, y, bounds.maxX, y);
        VectRelationProcessor processor = new VectRelationProcessor(tolerance, x, y);
        lineIndex.forInteracting(selection, tolerance, processor);
        return processor.getRelation() | Relation.A_OUTSIDE_B;
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
            ret = isConvex(vects, Tolerance.ZERO);
            convex = ret;
        }
        return ret;
    }

    /**
     * Determine if the linear ring represented by the vectList given contains only convex angles.
     * Results are undefined if the edges are unclosed or self intersect
     *
     * @param vects
     * @param accuracy
     * @return
     * @throws IndexOutOfBoundsException if the vect list does not have at least 4 elements
     */
    public static boolean isConvex(VectList vects, Tolerance accuracy) throws IndexOutOfBoundsException {
        int s = vects.size();
        double ax = vects.getX(s - 2);
        double ay = vects.getY(s - 2);
        double bx = vects.getX(0);
        double by = vects.getY(0);
        for (int i = 1; i < vects.size(); i++) {
            double cx = vects.getX(i);
            double cy = vects.getY(i);
            if (Line.counterClockwise(ax, ay, cx, cy, bx, by, accuracy) == -1) {
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
    
    /**
     * Toggle this ring in the network given - any links which did not exist are added, and any which
     * did exist are removed
     *
     * @param network
     */
    public void toggleTo(Network network){
        network.toggleAllLinks(vects);
    }
    
    @Override
    public Geom buffer(double amt, Linearizer linearizer, Tolerance accuracy) {
        if (amt == 0) {
            return this;
        }else if(amt < 0){
            Rect bounds = getBounds();
            if(Math.abs(amt) > Math.max(bounds.getWidth(), bounds.getHeight())){
                return null;
            }
        }
        VectList buffer = getEdgeBuffer(amt, linearizer, accuracy);
        return buildGeomFromRing(buffer, accuracy);
    }

    static Geom buildGeomFromRing(VectList closedRing, Tolerance accuracy){
        
        Area union = null;
        Area less = null;
        Network touching = null;
        
        //Sanitize the ring using a network
        Network network = new Network();
        network.addAllLinks(closedRing);
        network.explicitIntersections(accuracy);
        network.snap(accuracy);
        closedRing = parsePathFromNetwork(network, closedRing, accuracy);

        VectMap<Integer> indices = new VectMap<>();
        for(int i = 0; i < closedRing.size(); i++){
            double x = closedRing.getX(i);
            double y = closedRing.getY(i);
            final Integer index = indices.get(x, y);
            if(index != null){
                final int numVects = i - index;
                if(numVects == 2){
                    if(touching == null){
                        touching = new Network();
                    }
                    touching.addLink(x, y, closedRing.getX(i-1), closedRing.getY(i-1));
                    closedRing.removeAll(index, 2);
                    i -= 2;
                }else{
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
            }
            VectList links = network.map.get(x, y);
            if((i == 0) || (links.size() != 2)){
                indices.put(x, y, i);
            }
        }
        GeoShape ret = null;
        if(touching != null){
            ret = LineSet.valueOfInternal(touching).toGeoShape();
        }
        if(union != null){
            ret = (ret == null) ? union.toGeoShape() : union.toGeoShape().union(ret, accuracy);
        }
        if((ret != null) && (less != null)){
            ret = ret.less(less.toGeoShape(), accuracy);
        }
        return (ret == null) ? null : ret.simplify();
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
        double tolSq = tolerance.toleranceSq;
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
                    if(Line.distLineVectSq(a.getX(), a.getY(), b.getX(), b.getY(), c.getX(), c.getY()) <= tolSq){
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
        if(Relation.isDisjoint(node.relate(x, y, tolerance))){
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
    public VectList getEdgeBuffer(double amt, Linearizer linearizer, Tolerance tolerance) {
        Vect.check(amt, "Invalid amt {0}");
        if (amt == 0) {
            return vects.clone();
        }
        return getEdgeBuffer(vects, amt, linearizer, tolerance);
    }

    //The buffer produced by this may be self overlapping, and will need to be cleaned in a network before use
    static VectList getEdgeBuffer(VectList vects, double amt, Linearizer linearizer, Tolerance tolerance) {
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
            LineString.projectOutward(ax, ay, bx, by, cx, cy, amt, linearizer, tolerance, vect, result);
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
        double _area = getArea(transformed);
        if(_area < 0){
            transformed.reverse();
            _area = -_area;
        }
        Ring ring = new Ring(transformed, _area);
        return ring;
    }

    @Override
    public PathIter iterator() {
        return new PathIter() {

            final int max = vects.size() - 1;
            int index;

            @Override
            public boolean isDone() {
                return index > max;
            }

            @Override
            public void next() {
                index++;
            }

            @Override
            public PathSegType currentSegment(double[] coords) {
                coords[0] = vects.getX(index);
                coords[1] = vects.getY(index);
                if (index == 0) {
                    return PathSegType.MOVE;
                } else if (index == max) {
                    return PathSegType.CLOSE;
                } else {
                    return PathSegType.LINE;
                }
            }

        };
    }

    @Override
    public Ring clone() {
        return this;
    }

    @Override
    public GeoShape toGeoShape(Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
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
    public void addTo(Network network, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
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
    public int relate(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException{
        if(other instanceof Ring){
            return relate((Ring)other, accuracy);
        }
        return toArea().relate(other, linearizer, accuracy);
    }
    
    public int relate(Ring other, Tolerance accuracy) throws NullPointerException{
        int ret = NetworkRelationProcessor.relate(this, other, Linearizer.DEFAULT, accuracy); // Lines per quadrant is not used as both shapes are linear
        if((ret & Relation.A_INSIDE_B) == Relation.A_INSIDE_B){
            ret |= Relation.B_INSIDE_A;
        }
        if((ret & Relation.B_INSIDE_A) == Relation.B_INSIDE_A){
            ret |= Relation.A_INSIDE_B;
        }
        if(ret == Relation.TOUCH){ // All points touch 
            ret |= Relation.A_INSIDE_B;
            ret |= Relation.B_INSIDE_A;
        }
        return ret;
    }
    
    @Override
    public Geom union(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        return toArea().union(other, linearizer, accuracy);
    }

    @Override
    public Geom intersection(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        if (Relation.isDisjoint(getBounds().relate(other.getBounds(), accuracy))) {
            return null;
        }
        return toArea().intersection(other, linearizer, accuracy);
    }

    @Override
    public Geom less(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        if (Relation.isDisjoint(getBounds().relate(other.getBounds(), accuracy))) {
            return this;
        }
        Area area = toArea().less(other, linearizer, accuracy);
        return (area == null) ? null : area.simplify();
    }
    
    @Override
    public Geom xor(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        return toArea().xor(other, linearizer, accuracy);
    }
    
    /**
     * Get a point which is inside this linear ring. at least the distance given from an edge.
     * The details of which point are undefined.
     * If no part of the ring given is the required distance from its edge, return null
     * @param accuracy
     * @return Vect
     */
    public Vect getInternalPoint(Tolerance accuracy){
        if(isConvex()){
            return getCentroid();
        }
        return getInternalPoint(accuracy, getLineIndex());
    }

    Vect getInternalPoint(Tolerance accuracy, SpatialNode<Line> _lineIndex){
        IntersectionProcessor intersectionProcessor = new IntersectionProcessor(accuracy);
        ClosestLineProcessor closestLineProcessor = new ClosestLineProcessor();
        VectList intersections = new VectList();
        Rect bounds = getBounds();
        final double tolSq = accuracy.toleranceSq;
        double bx = vects.getX(0);
        double by = vects.getY(0);
        for(int i = vects.size()-1; i-- > 0;){
            double ax = vects.getX(i);
            double ay = vects.getY(i);
            
            //check normal from mid point of ab
            double mx = (ax + bx) / 2;
            double my = (ay + by) / 2;
            
            double dx = ay - by;
            double dy = bx - ax;

            int mul = (int)Math.ceil(Math.min(Math.abs(bounds.getWidth() / dx), Math.abs(bounds.getHeight() / dy)));
            dx = dx * mul + mx;
            dy = dy * mul + my;
            Line ray = new Line(mx, my, dx, dy);
            
            intersectionProcessor.reset(ray);            
            _lineIndex.forInteracting(ray.getBounds(), accuracy, intersectionProcessor);
            intersectionProcessor.getIntersections(intersections);
            
            if(intersections.size() > 1){
                Vect mid = Vect.valueOf((intersections.getX(1) + mx) / 2, (intersections.getY(1) + my) / 2);
                closestLineProcessor.reset(mid);
                _lineIndex.forInteracting(mid.getBounds(), accuracy, closestLineProcessor);

                if(closestLineProcessor.getMinDistSq() > tolSq){
                    return mid;
                }
            }
            
            bx = ax;
            by = ay;
        }
        return null;
    }
    
    public Ring convexHull(Tolerance accuracy){
        if(isConvex()){
            return this;
        }
        ConvexHull convexHull = new ConvexHull(accuracy);
        VectList ret = convexHull.getConvexHull(vects);
        Ring ring = new Ring(ret, null, null, null, null, Boolean.TRUE);
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
        StringBuilder str = new StringBuilder();
        new RingHandler().render(this, new GeomJaysonWriter(str));
        return str.toString();
    }
}
