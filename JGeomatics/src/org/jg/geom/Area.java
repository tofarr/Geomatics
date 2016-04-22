package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.jg.algorithm.ConvexHull;
import org.jg.geom.Network.LinkProcessor;
import org.jg.geom.Network.VertexProcessor;
import org.jg.util.RTree;
import org.jg.util.SpatialNode;
import org.jg.util.SpatialNode.NodeProcessor;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.VectList;
import org.jg.util.VectMap.VectMapProcessor;
import org.jg.util.VectSet;

/**
 *
 * @author tofar_000
 */
public class Area implements Geom {
    
    public static final Area[] NO_CHILDREN = new Area[0];

    final Ring shell;
    final Area[] children;
    Rect bounds;
    SpatialNode<Line> lineIndex;
    Double area;

    Area(Ring shell, Area... children) {
        this.shell = shell;
        this.children = children;
    }

    public Area(Ring shell) throws NullPointerException {
        this(shell, NO_CHILDREN);
        if(shell == null){
            throw new NullPointerException();
        }
    }
    
    public static Area valueOf(Tolerance accuracy, double... ords) throws NullPointerException,IllegalArgumentException{
        return valueOf(accuracy, new VectList(ords));
    }
    
    public static Area valueOf(Tolerance accuracy, VectList vects) throws NullPointerException,IllegalArgumentException{
        Network network = new Network();
        network.addAllLinks(vects);
        network.explicitIntersections(accuracy);
        network.snap(accuracy);
        return valueOfInternal(accuracy, network);
    }
    
    public static Area valueOf(Tolerance accuracy, Network network) {
        network = network.clone();
        network.explicitIntersections(accuracy);
        network.snap(accuracy);
        return valueOfInternal(accuracy, network);
    }
    
    static Area valueOfInternal(Tolerance accuracy, Network network) {
        List<Ring> rings = Ring.parseAllInternal(network, accuracy, true);
        return valueOfInternal(rings);
    }
    
    static Area valueOfInternal(List<Ring> rings){
        switch(rings.size()){
            case 0:
                return null;
            case 1:
                return rings.get(0).toArea();
            default:
                Ring[] ringArray = rings.toArray(new Ring[rings.size()]);
                Arrays.sort(ringArray, COMPARATOR);
                AreaBuilder builder = new AreaBuilder(null);
                for (Ring ring : ringArray) {
                    builder.add(ring);
                }
                return builder.build();
        }
    }
    
    public double getArea() {
        if (shell == null) {
            double ret = 0;
            for (Area ringSet : children) {
                ret += ringSet.getArea();
            }
            return ret;
        } else {
            double ret = shell.getArea();
            for (Area ringSet : children) {
                ret -= ringSet.getArea();
            }
            return ret;
        }
    }
    
    @Override
    public double getArea(Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        return getArea();
    }

    @Override
    public Rect getBounds() {
        if(shell == null){
            RectBuilder allBounds = new RectBuilder();
            for (Area ringSet : children) {
                allBounds.add(ringSet.getBounds());
            }
            return allBounds.build();
        }else{
            return shell.getBounds();
        }
    }

    @Override
    public Area transform(Transform transform) throws NullPointerException {
        if(transform.mode == Transform.NO_OP){
            return this;
        }
        Ring transformedShell = (shell == null) ? null : shell.transform(transform);
        Area[] transformedChildren = (children.length == 0) ? NO_CHILDREN : new Area[children.length];
        for (int c = 0; c < transformedChildren.length; c++) {
            transformedChildren[c] = children[c].transform(transform);
        }
        Arrays.sort(transformedChildren, COMPARATOR);
        return new Area(transformedShell, transformedChildren);
    }
    
    public Geom simplify(){
        return (children.length == 0) ? shell : this;
    }

    @Override
    public PathIterator pathIterator() {
        return new PathIterator() {

            final List<Ring> rings = getRings();
            int ringIndex;
            VectList vects = rings.get(0).vects;
            int vectIndex;
            int last = vects.size() - 1;

            @Override
            public int getWindingRule() {
                return WIND_NON_ZERO;
            }

            @Override
            public boolean isDone() {
                return (ringIndex >= rings.size());
            }

            @Override
            public void next() {
                if (vectIndex == last) {
                    ringIndex++;
                    if(ringIndex < rings.size()){
                        vects = rings.get(ringIndex).vects;
                        vectIndex = 0;
                       last = vects.size() - 1;
                    }else{
                        vects = null;
                    }
                } else {
                    vectIndex++;
                }
            }

            @Override
            public int currentSegment(float[] coords) {
                coords[0] = (float) vects.getX(vectIndex);
                coords[1] = (float) vects.getY(vectIndex);
                if (vectIndex == 0) {
                    return SEG_MOVETO;
                } else if (vectIndex == last) {
                    return SEG_CLOSE;
                } else {
                    return SEG_LINETO;
                }
            }

            @Override
            public int currentSegment(double[] coords) {
                coords[0] = vects.getX(vectIndex);
                coords[1] = vects.getY(vectIndex);
                if (vectIndex == 0) {
                    return SEG_MOVETO;
                } else if (vectIndex == last) {
                    return SEG_CLOSE;
                } else {
                    return SEG_LINETO;
                }
            }
        };
    }

    @Override
    public Area clone() {
        return this;
    }

    public int numRings() {
        int ret = (shell == null) ? 0 : 1;
        for (Area child : children) {
            ret += child.numRings();
        }
        return ret;
    }

    public int numLines() {
        int ret = 0;
        if (shell != null) {
            ret += shell.numLines();
        }
        for (Area child : children) {
            ret += child.numLines();
        }
        return ret;
    }

    public int numVects() {
        int ret = 0;
        if (shell != null) {
            ret += shell.numVects();
        }
        for (Area child : children) {
            ret += child.numVects();
        }
        return ret;
    }

    public SpatialNode<Line> getLineIndex() {
        SpatialNode<Line> ret = lineIndex;
        if (ret == null) {
            Rect[] allBounds = new Rect[numLines()];
            Line[] allLines = new Line[allBounds.length];
            addLines(allBounds, allLines, 0);
            RTree<Line> tree = new RTree<>(allBounds, allLines);
            ret = tree.getRoot();
            lineIndex = ret;
        }
        return ret;
    }

    private int addLines(Rect[] bounds, Line[] lines, int index) {
        if (shell != null) {
            VectList vects = shell.vects;
            int s = vects.size() - 1;
            for (int i = 0; i < s; i++) {
                Line line = vects.getLine(i);
                bounds[index] = line.getBounds();
                lines[index] = line;
                index++;
            }
        }
        for (Area child : children) {
            index = child.addLines(bounds, lines, index);
        }
        return index;
    }

    public List<Ring> getRings() {
        List<Ring> ret = new ArrayList<>();
        getRings(ret);
        return ret;
    }

    public void getRings(Collection<Ring> result) {
        if (shell != null) {
            result.add(shell);
        }
        for (Area child : children) {
            child.getRings(result);
        }
    }

    public VectSet getVects(VectSet result) {
        if (shell != null) {
            result.addAll(shell.vects);
        }
        for (Area child : children) {
            child.getVects(result);
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        toString(str);
        return str.toString();
    }

    @Override
    public void toString(Appendable appendable) throws NullPointerException, GeomException {
        try {
            appendable.append("[\"AR\"");
            toStringInternal(appendable);
            appendable.append("]");
        } catch (IOException ex) {
            throw new GeomException("Error writing", ex);
        }
    }

    private void toStringInternal(Appendable appendable) throws IOException {
        if(shell != null){
            appendable.append("[");
            shell.vects.toString(appendable);
        }
        for (int c = 0; c < children.length; c++) {
            appendable.append(',');
            children[c].toStringInternal(appendable);
        }
        if(shell != null){
            appendable.append("]");
        }
    }

    public void addTo(Network network) throws NullPointerException, IllegalArgumentException {
        if (shell != null) {
            shell.addTo(network);
        }
        for (int c = 0; c < children.length; c++) {
            children[c].addTo(network);
        }
    }

    @Override
    public void addTo(Network network, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        addTo(network);
    }

    @Override
    public GeoShape toGeoShape(Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        return toGeoShape();
    }
    
    public GeoShape toGeoShape(){
        return new GeoShape(this, null, null);
    }

    @Override
    public Geom buffer(double amt, Tolerance flatness, Tolerance accuracy) throws IllegalArgumentException, NullPointerException {
        if(amt == 0){
            return this;
        }
        if(shell == null){
            Geom result = null;
            for(Area child : children){
                Geom childResult = child.buffer(amt, flatness, accuracy);
                if(childResult != null){
                    result = (result == null) ? childResult : result.union(childResult, flatness, accuracy);
                }
            }
            return result;
        }else{
            Geom result = shell.buffer(amt, flatness, accuracy);
            if(result == null){
                return null; // buffered out of existence!
            }
            amt = -amt;
            for(Area child : children){
                Geom childResult = child.buffer(amt, flatness, accuracy);
                if(childResult != null){ // child may have been buffered out of existance!
                    result = result.less(childResult, flatness, accuracy);
                    if(result == null){ // buffered out of existence!
                        return null;
                    }
                }
            }
            return result;
        }
    }

    @Override
    public int relate(Vect vect, Tolerance tolerance) throws NullPointerException {
        return relateInternal(vect.x, vect.y, tolerance);
    }

    @Override
    public int relate(VectBuilder vect, Tolerance tolerance) throws NullPointerException {
        return relateInternal(vect.getX(), vect.getY(), tolerance);
    }

    public int relate(double x, double y, Tolerance tolerance) throws IllegalArgumentException, NullPointerException {
        Vect.check(x, y);
        return relateInternal(x, y, tolerance);
    }

    int relateInternal(double x, double y, Tolerance tolerance) throws IllegalArgumentException, NullPointerException {
        return Ring.relateInternal(x, y, getLineIndex(), tolerance);
    }

    @Override
    public int relate(final Geom geom, Tolerance flatness, final Tolerance accuracy) throws NullPointerException {
        if(geom instanceof Ring){
            return relate((Ring)geom, accuracy);
        }else if(geom instanceof Area){
            return relate((Area)geom, accuracy);
        }else{
            return toGeoShape().relate(geom, flatness, accuracy);
        }
    }
    
    public int relate(Area area, Tolerance accuracy){
        if(area.shell == null){
            int ret = Relation.NULL;
            for(Area child : area.children){
                ret |= child.relate(child, accuracy);
            }
            return ret;
        }
        int ret = relate(area.shell, accuracy);
        int inverse = Relation.NULL;
        for(Area child : area.children){
            inverse |= child.relate(child, accuracy);
        }
        ret |= Relation.invert(inverse);
        return ret;
            
    }
    
    public int relate(Ring ring, Tolerance accuracy){
        if(ring == null){
            int ret = Relation.NULL;
            for(Area child : children){
                ret |= child.relate(ring, accuracy);
            }
            return ret;
        }
        int ret = shell.relate(shell, accuracy);
        int inverse = Relation.NULL;
        for(Area child : children){
            inverse |= child.relate(ring, accuracy);
        }
        ret |= Relation.invert(inverse);
        return ret;
    }

    private static void addDisjoint(Area area, List<Area> results) {
        if (area.shell == null) {
            results.addAll(Arrays.asList(area.children));
        } else {
            results.add(area);
        }
    }
        
    @Override
    public Geom union(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        if (other instanceof Area) {
            return union((Area) other, accuracy).simplify();
        } else if (other instanceof Ring) {
            return union((Ring) other, accuracy).simplify();
        }
        return toGeoShape().union(other, flatness, accuracy);
    }

    public Area union(Area other, Tolerance accuracy) {
        if (Relation.isDisjoint(getBounds().relate(other.getBounds(), accuracy))) { // Skip networking polygonization - shortcut
            final List<Area> areas = new ArrayList<>();
            addDisjoint(this, areas);
            addDisjoint(other, areas);
            Area[] children = areas.toArray(new Area[areas.size()]);
            Arrays.sort(children, COMPARATOR);
            return new Area(null, children);
        }
        Network network = Network.valueOf(accuracy, accuracy, this, other);
        return unionInternal(this, other, network, accuracy);
    }

    //Assumes this and other are normalized against each other - no unspecified points of intersection
    Area unionNormalized(Area other, Tolerance accuracy) {
        if (Relation.isDisjoint(getBounds().relate(other.getBounds(), accuracy))) { // Skip networking polygonization - shortcut
            final List<Area> areas = new ArrayList<>();
            addDisjoint(this, areas);
            addDisjoint(other, areas);
            Area[] children = areas.toArray(new Area[areas.size()]);
            Arrays.sort(children, COMPARATOR);
            return new Area(null, children);
        }
        Network network = new Network();
        addTo(network);
        other.addTo(network);
        return unionInternal(this, other, network, accuracy);
    }
    
    static Area unionInternal(final Area a, final Area b, final Network network, final Tolerance accuracy){
        final Network union = new Network();
        final VectBuilder workingVect = new VectBuilder();
        final boolean[] touching = new boolean[1];
        network.forEachLink(new LinkProcessor(){
            @Override
            public boolean process(double ax, double ay, double bx, double by) {
                workingVect.set((ax + bx) / 2, (ay + by) / 2);
                int aRelate = a.relate(workingVect, accuracy);
                if(Relation.isBInsideA(aRelate)){
                    return true;
                }
                int bRelate = b.relate(workingVect, accuracy);
                if(Relation.isBInsideA(bRelate)){
                    return true;
                }
                if(Relation.isTouch(aRelate) && Relation.isTouch(bRelate)){
                    touching[0] = true;
                }
                union.addLinkInternal(ax, ay, bx, by);
                return true;
            }
        
        });
        
        List<Ring> rings = Ring.parseAllInternal(union, accuracy, true);
        
        if((!touching[0]) || (rings.size() == 1)){ // there were no touching allLines - we are done!
            return valueOfInternal(rings);
        }
        
        union.clear();
        for(Ring ring : rings){
            union.toggleAllLinks(ring.vects);
        }
        
        rings = Ring.parseAllInternal(union, accuracy, false);
        return valueOfInternal(rings);
    }

    public Area union(Ring other, Tolerance accuracy) {
        return union(other.toArea(), accuracy);
    }
    
    @Override
    public Geom intersection(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        if (other instanceof Area) {
            GeoShape ret = intersection((Area) other, accuracy);
            return (ret == null) ? null : ret.simplify();
        } else if (other instanceof Ring) {
            GeoShape ret = intersection((Ring) other, accuracy);
            return (ret == null) ? null : ret.simplify();
        }
        return toGeoShape().intersection(other, flatness, accuracy);
    }
    
    public GeoShape intersection(Ring other, Tolerance accuracy) throws NullPointerException{
        return intersection(other.toArea(), accuracy);
    }

    public GeoShape intersection(final Area other, final Tolerance accuracy) throws NullPointerException {       
        if (Relation.isDisjoint(getBounds().relate(other.getBounds(), accuracy))) { // Skip networking polygonization - shortcut
            return null;
        }
        final Network network = Network.valueOf(accuracy, accuracy, this, other);
        network.forEachVertex(new VertexProcessor(){
            @Override
            public boolean process(double x, double y, int numLinks) {
                if(Relation.isDisjoint(relateInternal(x, y, accuracy))
                    || Relation.isDisjoint(other.relateInternal(x, y, accuracy))){
                    network.removeVertexInternal(x, y);
                }
                return true;
            }
        });
        network.forEachLink(new LinkProcessor(){
            @Override
            public boolean process(double ax, double ay, double bx, double by) {
                double x = (ax + bx) / 2;
                double y = (ay + by) / 2;
                if(Relation.isDisjoint(relateInternal(x, y, accuracy))
                    || Relation.isDisjoint(other.relateInternal(x, y, accuracy))){
                    network.removeLinkInternal(ax, ay, bx, by);
                }
                return true;
            }
        
        });
        GeoShape ret = GeoShape.valueOfInternal(network, accuracy);
        return ret;
    }
    
    @Override
    public Area less(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        if (Relation.isDisjoint(getBounds().relate(other.getBounds(), accuracy))) { // Skip networking polygonization - shortcut
            return this;
        }
        if(other instanceof Ring){
            return less(((Ring)other).toArea(), accuracy);
        }else if(other instanceof Area){
            return less((Area)other, accuracy);
        }else{
            Area otherArea = other.toGeoShape(flatness, accuracy).area;
            if(otherArea == null){
                return this;
            }
            return less(otherArea, accuracy);
        }
    }

    public Area less(final Ring other, final Tolerance accuracy) throws NullPointerException {
        return less(other.toArea(), accuracy);
    }
    
    public Area less(final Area other, final Tolerance accuracy) throws NullPointerException {
        if (Relation.isDisjoint(getBounds().relate(other.getBounds(), accuracy))) { // Skip networking polygonization - shortcut
            return this;
        }
        final Network network = Network.valueOf(accuracy, accuracy, this, other);
        network.forEachVertex(new VertexProcessor(){
            @Override
            public boolean process(double x, double y, int numLinks) {
                if(Relation.isDisjoint(relateInternal(x, y, accuracy))
                        || Relation.isBInsideA(other.relateInternal(x, y, accuracy))){
                    network.removeVertexInternal(x, y);
                }
                return true;
            }
        });
        final Network otherNetwork = new Network();
        final boolean[] touching = new boolean[1];
        network.forEachLink(new LinkProcessor(){
            @Override
            public boolean process(double ax, double ay, double bx, double by) {
                double x = (ax + bx) / 2;
                double y = (ay + by) / 2;
                int relate = relateInternal(x, y, accuracy);
                int otherRelate = other.relateInternal(x, y, accuracy);
                if(Relation.isDisjoint(relate)
                    || Relation.isBInsideA(otherRelate)){
                    network.removeLinkInternal(ax, ay, bx, by);
                }
                if(Relation.isTouch(otherRelate)){
                    otherNetwork.addLinkInternal(ax, ay, bx, by);
                    if(Relation.isTouch(relate)){
                        touching[0] = true;
                    }
                }
                return true;
            }
        });
        
        if(!touching[0]){ // No common allLines, so make area and be done
            return Area.valueOfInternal(accuracy, network);
        }
        
        List<Ring> rings = Ring.parseAllInternal(network, accuracy, true);
        for(int i = rings.size(); i-- > 0;){
            Ring ring = rings.get(i);
            int index = ring.numVects()-1;
            double bx = ring.getX(index);
            double by = ring.getY(index);
            boolean discard = true;
            while(index-- != 0){
                double ax = ring.getX(index);
                double ay = ring.getY(index);
                if(!otherNetwork.hasLink(ax, ay, bx, by)){
                    discard = false;
                    break;
                }
                bx = ax;
                by = ay;
            }
            if(discard){
                rings.remove(i);
            }
        }

        return Area.valueOfInternal(rings);
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.shell);
        hash = 71 * hash + Arrays.deepHashCode(this.children);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Area)) {
            return false;
        }
        Area other = (Area) obj;
        return Objects.equals(this.shell, other.shell)
                && Arrays.equals(this.children, other.children);
    }

    public int getDepth() {
        int ret = 0;
        for(Area child : children){
            ret = Math.max(ret, child.getDepth());
        }
        if(shell != null){
            ret++;
        }
        return ret;
    }
    
    public int numChildren(){
        return children.length;
    }
    
    public Area getChild(int index){
        return children[index];
    }
    
    void getVects(VectList results){
        if(shell != null){
            results.addAll(shell.vects);
        }
        for(Area child : children){
            child.getVects(results);
        }
    }
    
    public Ring getConvexHull(Tolerance accuracy){
        if((shell != null) && shell.isConvex()){
            return shell;
        }
        VectList vects = new VectList(numVects());
        getVects(vects);
        ConvexHull hull = new ConvexHull(accuracy);
        VectList convexHull = hull.getConvexHull(vects);
        return new Ring(convexHull, null);
    }
      
    /**
     * Bisect this area along the line given. Returns either one or two areas, depending on whether
     * the line actually bisected this area.
     */
    public List<Area> bisect(Line bisector, Tolerance accuracy) throws NullPointerException {
        List<Area> ret = new ArrayList<>();
        bisector = bisector.segCrossing(getBounds());
        bisectInternal(bisector, accuracy, ret);
        return ret;
    }
    
    
    void bisectInternal(final Line bisector, final Tolerance accuracy, List<Area> results){
        final Network networkA = new Network();
        final Network networkB = new Network();
        
        getLineIndex().forEach(new NodeProcessor<Line>(){ //process all lines...
            final VectBuilder workingVect = new VectBuilder();
            @Override
            public boolean process(Rect bounds, Line line) {
                line.getA(workingVect);
                int a = bisector.counterClockwise(workingVect, accuracy);
                line.getB(workingVect);
                int b = bisector.counterClockwise(workingVect, accuracy);
                
                if(a > 0){
                    if(b >= 0){
                        networkA.addLink(line); // if both parts of line are left of line, add to a
                    }else{ // if both parts of line are on separate sides of line, split line between left and right
                        bisector.intersectionLine(line, accuracy, workingVect);
                        networkA.addLinkInternal(line.ax, line.ay, workingVect.getX(), workingVect.getY());
                        networkB.addLinkInternal(line.bx, line.by, workingVect.getX(), workingVect.getY());
                    }
                }else if(a < 0){
                    if(b <= 0){
                        networkB.addLink(line); // if both parts of line are right of line, add to b
                    }else{ // if both parts of line are on separate sides of line, split line between left and right
                        bisector.intersectionLine(line, accuracy, workingVect);
                        networkB.addLinkInternal(line.ax, line.ay, workingVect.getX(), workingVect.getY());
                        networkA.addLinkInternal(line.bx, line.by, workingVect.getX(), workingVect.getY());
                    }
                }else if(b == 0){  // if both parts of line are on line, add to both
                    networkA.addLink(line);
                    networkB.addLink(line);
                }else if(b > 0){
                    networkA.addLink(line);
                }else if(b < 0){
                    networkB.addLink(line);
                }
                return true;
            }
        });
        
        //Link hanglines
        Network networkA2 = linkHangLines(networkA, accuracy);
        Network networkB2 = linkHangLines(networkB, accuracy);
        
        Area areaA = Area.valueOfInternal(accuracy, networkA2); //convert both networks to areas
        if(areaA != null){
            results.add(areaA);
        }
        Area areaB = Area.valueOfInternal(accuracy, networkB2);
        if(areaB != null){
            results.add(areaB);
        }
    }

    
    Network linkHangLines(Network network, Tolerance accuracy){
        
        //find all points in a with a single link
        final VectList hangPoints = new VectList();
        network.forEachVertex(new VertexProcessor(){
            @Override
            public boolean process(double x, double y, int numLinks) {
                if(numLinks == 1){
                    hangPoints.add(x, y);
                }
                return true;
            }
        
        });
        
        //sort by distance from bounds min min
        Rect _bounds = getBounds();
        hangPoints.sortByDist(_bounds.minX, _bounds.minY);

        //add links between hang points
        int index = hangPoints.size();
        while(--index > 0){
            double bx = hangPoints.getX(index);
            double by = hangPoints.getY(index);
            index--;
            double ax = hangPoints.getX(index);
            double ay = hangPoints.getY(index);
            network.addLinkInternal(ax, ay, bx, by);
        }

        network.explicitIntersections(accuracy);
        network.snap(accuracy);
        network.removeColinearPoints(accuracy); // prevents infinite loop because of rounding error
        return network;
    }
    
    
    public Ring largestConvexRing(final Tolerance accuracy) {
        if((shell != null) && shell.isConvex() && (children.length == 0)){
            return shell;
        }
        
        Network network = new Network();
        VectSet holeCentroidSet = new VectSet();
        getDisected(accuracy, network, holeCentroidSet);
        VectList holeCentroids = new VectList();
        holeCentroidSet.toList(holeCentroids);
                
        //start hunting for largest convex rings in result - this is a brute force search which may be slow.
        final Network processed = new Network();
        VectList allVects = network.getVects(new VectList()); // get all vectors in sorted order
        VectList largest = new VectList();
        VectList current = new VectList();
        VectSet currentSet = new VectSet();
        VectBuilder workingVect = new VectBuilder();
        double largestArea = -1;
        for(int a = 0; a < allVects.size(); a++){
            double ax = allVects.getX(a);
            double ay = allVects.getY(a);
            VectList links = network.map.get(ax, ay);
            for(int b = 0; b < links.size(); b++){
                double bx = links.getX(b);
                double by = links.getY(b);
                if(!processed.hasLink(ax, ay, bx, by)){
                    if(followConvexRing(ax, ay, bx, by, network, current, currentSet, workingVect, holeCentroids, accuracy)){
                        processed.addAllLinks(current);
                        double currentArea = Ring.getArea(current);
                        if(isAGreaterB(current, currentArea, largest, largestArea, accuracy)){
                            VectList tmp = current;
                            current = largest;
                            largest = tmp;
                            largestArea = currentArea;
                        }
                    }
                }
            }
        }
        
        //Simplify here
        int minIndex = Ring.minIndex(largest);
        if(minIndex > 0){
            largest = Ring.rotate(largest, minIndex);
        }
        largest = LineString.excludeColinear(largest, accuracy);
        
        return new Ring(largest, largestArea);
    }
    
    private boolean isAGreaterB(VectList a, double areaA, VectList b, double areaB, Tolerance accuracy){
        int c = accuracy.check(areaA - areaB);
        if(c > 0){
            return true;
        }else if(c == 0){
            return a.getBounds().getArea() < b.getBounds().getArea();
        }else{
            return false;
        }
    }
    
    void getDisected(final Tolerance accuracy, final Network networkOut, final VectSet holeCentroidsOut){
        final Ring convexHull = getConvexHull(accuracy); // get convex hull
        final Network network = new Network(); // add to network
        addTo(network);
        addTo(networkOut);
        final Rect _bounds = getBounds();
        networkOut.map.forEach(new VectMapProcessor<VectList>(){
            final SpatialNode<Line> lineIndex = getLineIndex();
            final IntersectionProcessor processor = new IntersectionProcessor(accuracy);
            final VectList intersections = new VectList();
            @Override
            public boolean process(double ax, double ay, VectList links) {
                if(Relation.isBInsideA(convexHull.relate(ax, ay, accuracy))){ //if point is inside convex ring...
                    for(int b = links.size(); b-- > 0;){
                        double bx = links.getX(b);
                        double by = links.getY(b);
                        Line link = new Line(ax, ay, bx, by);
                        link = link.segCrossing(_bounds);
                        network.addLink(link);
                        if(b > 0){
                            
                            double dx = bx - ax;
                            double dy = by - ay;
                            double dDst = Math.sqrt((dx * dx) + (dy * dy));

                            double ex = links.getX(b - 1) - ax;
                            double ey = links.getY(b - 1) - ay;
                            double eDst = Math.sqrt((ex * ex) + (ey * ey));

                            double fx = (dx / dDst) + (ex / eDst);
                            double fy = (dy / dDst) + (ey / eDst);

                            if ((fx != 0) || (fy != 0)) {
                                link = new Line(ax, ay, fy + ax, ay - fx);
                                link = link.segCrossing(_bounds);
                                network.addLink(link);
                            }
                        }
                    }
                }
                return true;
            }
        });
        network.explicitIntersections(accuracy); //explicitise and snap
        network.snap(accuracy);
        
        network.removeHangLines(); // remove hang lines from network
        
        //this network contains a bunch of convex features. some of these may be holes - if holes are present these effect
        //the convex region calculation and should be accounted for. We therefore need to identify which of these convex
        //regions are holes.
        populateHoleCentroids(network, holeCentroidsOut, accuracy);
        
        //Remove any points outside this...
        network.map.forEach(new VectMapProcessor<VectList>(){
            @Override
            public boolean process(double x, double y, VectList value) {
                if(Relation.isBOutsideA(_bounds.relateInternal(x, y, accuracy))){
                    network.removeVertexInternal(x, y);
                }
                return true;
            }
        });
        
        //Remove any points and lines outside this outside this
        networkOut.clear();
        network.forEachLink(new LinkProcessor(){
            @Override
            public boolean process(double ax, double ay, double bx, double by) {
                double x = (ax + bx) / 2;
                double y = (ay + by) / 2;
                if(!Relation.isBOutsideA(relateInternal(x, y, accuracy))){
                    networkOut.addLinkInternal(ax,ay,bx,by);
                }
                return true;
            }
        });
    }
    
    //for each point in network...
    //store set of links
    //build rings, always going left (stop when get back to origin, remove last connector from set of available.
    //get ring centroid
    //test if centroid outside - if so add it to holes
    private void populateHoleCentroids(final Network network, final VectSet holeCentroidsOut, final Tolerance accuracy){
        if(children.length == 0){
            return; // no children means no holes, so we can skip the rest of the logic
        }
        //NOT WORKING OR INEFFICIENT
        network.map.forEach(new VectMapProcessor<VectList>(){
            final VectList path = new VectList();
            final VectBuilder workingVect = new VectBuilder();
            final Set<Line> done = new HashSet<>();
            @Override
            public boolean process(double ax, double ay, VectList links) { // for each point
                final double ox = ax; // store origin - we need it to know when we have reached the end of a closed ring
                final double oy = ay;
                for(int b = links.size(); b-- > 0;){
                    ax = ox;
                    ay = oy;
                    double bx = links.getX(b);
                    double by = links.getY(b);
                    Line line = new Line(ax, ay, bx, by);
                    if(!done.contains(line)){
                        path.clear();
                        path.add(ax, ay);
                        path.add(bx, by);
                        done.add(line);
                        while((bx != ox) || (by != oy)){ // follow path always taking leftmost turn to get a closed linear ring
                            network.nextCW(bx, by, ax, ay, workingVect);
                            ax = bx;
                            ay = by;
                            bx = workingVect.getX();
                            by = workingVect.getY();
                            path.add(bx, by);
                            done.add(new Line(ax, ay, bx, by));
                        }
                        if(Ring.getArea(path) > 0){ // only counter clockwise paths are valid
                            Vect centroid = Ring.getCentroid(path);
                            if(Relation.isBOutsideA(relate(centroid, accuracy))){ // If the centroid is outside, then this is a hole!
                                holeCentroidsOut.add(centroid);
                            }
                        }
                    }
                }
                return true;
            }
        });
    }
    
    private boolean followConvexRing(double ax, double ay, double bx, double by, Network network, VectList current,
            VectSet currentSet, VectBuilder workingVect, VectList holeCentroids, Tolerance accuracy) {
        final double ox = ax;
        final double oy = ay;
        current.clear();
        current.add(ax, ay);
        current.add(bx, by);
        currentSet.clear();
        currentSet.add(ax, ay);
        currentSet.add(bx, by);
        double cx = ax;
        double cy = ay;
        boolean removeFromSet = false;
        while(true){ //traverse ring adding points to current. Do not allow any right turns.
            
            while(true){
                network.nextCCW(bx, by, cx, cy, workingVect);
                
                if((workingVect.getX() == ax) && (workingVect.getY() == ay)){
                    removeFromSet = true;
                    break; //current should be removed
                }
                
                //Calculate number determining if point is left, right, or on line, stop only when not a right turn
                int val = accuracy.check((workingVect.getX() - ax) * (by - ay) - (workingVect.getY() - ay) * (bx - ax));
                if(val <= 0){
                    ax = bx;
                    ay = by;
                    bx = workingVect.getX();
                    by = workingVect.getY();
                    current.add(bx, by);
                    break;
                }
                cx = workingVect.getX();
                cy = workingVect.getY();
            }
            
            int originRight = accuracy.check((ox - ax) * (by - ay) - (oy - ay) * (bx - ax));
            
            if((originRight > 0) || currentSet.contains(bx, by)){
                if((current.getX(0) == bx) && (current.getY(0) == by)){
                    // looped back to start - check that the final triangle is convex...
                    originRight = accuracy.check((ox - ax) * (current.getY(1) - ay) - (oy - ay) * (current.getX(1) - ax));
                    boolean valid = (originRight >= 0);
                    
                    //check if we contain any hole centroids
                    for(int h = holeCentroids.size(); valid && (h-- > 0);){
                        if(Relation.isBInsideA(Ring.convexRelate(current, holeCentroids.getX(h), holeCentroids.getY(h), accuracy))){
                            valid = false;
                        }
                    }
                    if(valid){
                        return true;
                    }
                }
                //Collided with self or looped inwards - take a step back and try again
                int index = current.size()-1;
                if(index < 2){
                    return false;
                }
                cx = current.getX(index);
                cy = current.getY(index);
                current.remove(index);
                if(removeFromSet){
                    currentSet.remove(cx, cy);
                    removeFromSet = false;
                }
                index--;
                bx = current.getX(index);
                by = current.getY(index);
                index--;
                ax = current.getX(index);
                ay = current.getY(index);
            }else{
                currentSet.add(bx, by);
                cx = ax;
                cy = ay;
            }
        }
    }
  
    

    static class AreaBuilder {

        final Ring shell;
        final ArrayList<AreaBuilder> children;

        AreaBuilder(Ring shell) {
            this.shell = shell;
            children = new ArrayList<>();
        }

        Area build() {
            if (shell == null) {
                if (children.size() == 1) {
                    return children.get(0).build();
                }
            }
            Area[] _children = new Area[children.size()];
            for (int c = 0; c < _children.length; c++) {
                _children[c] = children.get(c).build();
            }
            Area ret = new Area(shell, _children);
            return ret;
        }

        boolean add(Ring ring) {
            if (!canAdd(ring)) {
                return false;
            }
            for (AreaBuilder child : children) {
                if (child.add(ring)) {
                    return true;
                }
            }
            children.add(new AreaBuilder(ring));
            return true;
        }

        boolean canAdd(Ring ring) {
            if (shell == null) {
                return true;
            }
            int i = 0;
            while (true) {
                int relate = shell.relate(ring.vects.getX(i), ring.vects.getY(i), Tolerance.ZERO);
                if(Relation.isBInsideA(relate)){
                    return true;
                }else if(Relation.isBOutsideA(relate)){
                    return false;
                }
                i++;
            }
        }
    }

}
