package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.jg.algorithm.ConvexHull;
import org.jg.algorithm.Generalizer;
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
            getVects(results);
        }
    }
    
    public Ring getConvexHull(){
        if((shell != null) && shell.isConvex()){
            return shell;
        }
        VectList vects = new VectList(numVects());
        getVects(vects);
        VectList convexHull = ConvexHull.getConvexHull(vects);
        return new Ring(convexHull, null);
    }

    public Ring largestConvexRing(Tolerance accuracy){
        
        Ring convexHull = getConvexHull();
        if(convexHull == shell){
            return convexHull;
        }
        
        Network network = new Network();
        addTo(network);
 
        //find vertex in convex hull closest to centroid
        final Vect centroid = convexHull.getCentroid();
        final VectBuilder a = new VectBuilder();
        network.map.forEach(new VectMapProcessor<VectList>(){
            double minDistSq = Double.MAX_VALUE;
            @Override
            public boolean process(double x, double y, VectList value) {
                double distSq = Vect.distSq(x, y, centroid.x, centroid.y);
                if(distSq < minDistSq){
                    minDistSq = distSq;
                    a.set(x, y);
                }
                return true;
            }
            
        });
        
        //we have possibilities for bisecting our area...
            //we can bisect along a link line
            //we can bisect along the bisecting line of 2 link lines
            //we can bisect along the normal of the bisecting line of 2 link lines
        //We must choose the one which results in the largest inequality in the remaining area.
        
        VectList links = network.map.get(a); // there was always be at least 2 links
        VectBuilder b = new VectBuilder();
        VectBuilder c = new VectBuilder();
        VectBuilder d = new VectBuilder();
        links.getVect(0, c);
        Area largest = null;
        for(int i = links.size(); i-- > 0;){
            links.getVect(i, b);
            Area area = bisectAndGetLargest(a.getX(), a.getY(), b.getX(), b.getY(), accuracy);
            if((largest == null) || (area.getArea() > largest.getArea())){
                largest = area;
            }
            
            double dx = b.getX() - a.getX();
            double dy = b.getY() - a.getY();
            double dDst = Math.sqrt((dx * dx) + (dy * dy));
            
            double ex = c.getX() - a.getX();
            double ey = c.getY() - a.getY();
            double eDst = Math.sqrt((ex * ex) + (ey * ey));
            
            double fx = (dx / dDst) + (ex / eDst);
            double fy = (dy / dDst) + (ey / eDst);
            
            if((fx != 0) || (fy != 0)){
                //I could be wrong, but I cant think of any case where this would hold true
                //d.set(fx + a.getX(), fy + a.getY());
                //area = bisectAndGetLargest(a.getX(), a.getY(), d.getX(), d.getY(), accuracy);
                //if((largest == null) || (area.getArea() > largest.getArea())){
                //    largest = area;
                //}

                d.set(fy + a.getY(), fx - a.getX());
                area = bisectAndGetLargest(a.getX(), a.getY(), d.getX(), d.getY(), accuracy);
                if((largest == null) || (area.getArea() > largest.getArea())){
                    largest = area;
                }
            }
            
            VectBuilder tmp = b;
            b = c;
            c = tmp;
        }
        
        return largest.largestConvexRing(accuracy); // process may need to repeat
    }
    
    Area bisectAndGetLargest(final double ax, final double ay, final double nx, final double ny, final Tolerance accuracy){
        
        final Network networkA = new Network();
        final Network networkB = new Network();
        final double bx = nx - ax;
        final double by = ny - ay;
        getLineIndex().forEach(new NodeProcessor<Line>(){ //process all lines...
            final VectBuilder intersection = new VectBuilder();
            @Override
            public boolean process(Rect bounds, Line line) {
                double cx = line.ax - ax;
                double cy = line.ay - ay;
                int c = accuracy.check(cx *  by - cy * bx);
                
                double dx = line.bx - ax;
                double dy = line.by - ay;
                int d = accuracy.check(dx *  by - dy * bx);
                
                if(c > 0){
                    if(d >= 0){
                        networkA.addLink(line); // if both parts of line are left of line, add to a
                    }else{ // if both parts of line are on separate sides of line, split line between left and right
                        Line.intersectionLineInternal(ax, ay, nx, ny, line.ax, line.ay, line.bx, line.by, accuracy, intersection);
                        networkA.addLinkInternal(line.ax, line.ay, intersection.getX(), intersection.getY());
                        networkB.addLinkInternal(line.bx, line.by, intersection.getX(), intersection.getY());
                    }
                }else if(c < 0){
                    if(d <= 0){
                        networkB.addLink(line); // if both parts of line are right of line, add to b
                    }else{ // if both parts of line are on separate sides of line, split line between left and right
                        Line.intersectionLineInternal(ax, ay, nx, ny, line.ax, line.ay, line.bx, line.by, accuracy, intersection);
                        networkB.addLinkInternal(line.ax, line.ay, intersection.getX(), intersection.getY());
                        networkA.addLinkInternal(line.bx, line.by, intersection.getX(), intersection.getY());
                    }
                }else if(d == 0){  // if both parts of line are on line, add to both
                    networkA.addLink(line);
                    networkB.addLink(line);
                }else if(d > 0){
                    networkA.addLink(line);
                }else if(d < 0){
                    networkB.addLink(line);
                }
                return true;
            }
        
        });
        
        //Link hanglines
        Network networkA2 = linkHangLines(networkA, accuracy);
        Network networkB2 = linkHangLines(networkB, accuracy);
        
        Area areaA = Area.valueOfInternal(accuracy, networkA2); //convert both networks to areas
        Area areaB = Area.valueOfInternal(accuracy, networkB2);
        
        return (areaA.getArea() > areaB.getArea()) ? areaA : areaB; // return largest area
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
        Rect bounds = getBounds();
        for (int i = hangPoints.size(); i-- > 1;) {
            double ix = hangPoints.getX(i);
            double iy = hangPoints.getY(i);
            double disq = Vect.distSq(bounds.minX, bounds.minY, ix, iy);
            for (int j = i; j-- > 0;) {
                double jx = hangPoints.getX(j);
                double jy = hangPoints.getY(j);
                double djsq = Vect.distSq(bounds.minX, bounds.minY, jx, jy);
                if (disq < djsq) {
                    hangPoints.swap(i, j);
                    ix = jx;
                    iy = jy;
                    disq = djsq;
                }
            }
        }
        
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
        //douglas peucker here prevents infinite loop rounding error
        Generalizer generalizer = new Generalizer(accuracy);
        Network ret = generalizer.generalize(network);
        return ret;
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
