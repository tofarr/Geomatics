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
    public double getArea(Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
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
    public void addTo(Network network, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        addTo(network);
    }

    @Override
    public GeoShape toGeoShape(Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        return toGeoShape();
    }
    
    public GeoShape toGeoShape(){
        return new GeoShape(this, null, null);
    }

    @Override
    public Geom buffer(double amt, Linearizer linearizer, Tolerance accuracy) throws IllegalArgumentException, NullPointerException {
        if(amt == 0){
            return this;
        }
        if(shell == null){
            Geom result = null;
            for(Area child : children){
                Geom childResult = child.buffer(amt, linearizer, accuracy);
                if(childResult != null){
                    result = (result == null) ? childResult : result.union(childResult, linearizer, accuracy);
                }
            }
            return result;
        }else{
            Geom result = shell.buffer(amt, linearizer, accuracy);
            if(result == null){
                return null; // buffered out of existence!
            }
            amt = -amt;
            for(Area child : children){
                Geom childResult = child.buffer(amt, linearizer, accuracy);
                if(childResult != null){ // child may have been buffered out of existance!
                    result = result.less(childResult, linearizer, accuracy);
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
    public int relate(final Geom geom, Linearizer linearizer, final Tolerance accuracy) throws NullPointerException {
        if(geom instanceof Ring){
            return relate((Ring)geom, accuracy);
        }else if(geom instanceof Area){
            return relate((Area)geom, accuracy);
        }else{
            return toGeoShape().relate(geom, linearizer, accuracy);
        }
    }
    
    public int relate(Area area, Tolerance accuracy){
        if(area.shell == null){
            int ret = Relation.NULL;
            for(Area child : area.children){
                int childRelate = relate(child, accuracy); WRONG
                if(!Relation.isBOutsideA(childRelate)){ // fully contained within a child
                    childRelate |= Relation.A_OUTSIDE_B;
                    return childRelate; 
                }
                ret |= relate(child, accuracy);
                if(ret == Relation.ALL){
                    return ret;
                }
            }
            return ret;
        }
        int ret = relate(area.shell, accuracy);
        int inverse = Relation.NULL;
        for(Area child : area.children){
            int childRelate = relate(child, accuracy);
            if(!Relation.isBOutsideA(childRelate)){ // fully contained within a hole
                ret = Relation.invert(childRelate);
                ret |= Relation.A_OUTSIDE_B;
                return ret; 
            }
            inverse |= child.relate(child, accuracy);
            if(inverse == Relation.ALL){
                return inverse;
            }
        }
        ret |= Relation.invert(inverse);
        return ret;
            
    }
    
    public int relate(Ring ring, Tolerance accuracy){
        if(shell == null){
            int ret = Relation.NULL;
            for(Area child : children){
                ret |= child.relate(ring, accuracy);
            }
            return ret;
        }
        int ret = shell.relate(ring, accuracy);
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
    public Geom union(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        if (other instanceof Area) {
            return union((Area) other, accuracy).simplify();
        } else if (other instanceof Ring) {
            return union((Ring) other, accuracy).simplify();
        }
        return toGeoShape().union(other, linearizer, accuracy);
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
        Network network = Network.valueOf(accuracy, Linearizer.DEFAULT, this, other);
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
    public Geom intersection(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        if (other instanceof Area) {
            GeoShape ret = intersection((Area) other, accuracy);
            return (ret == null) ? null : ret.simplify();
        } else if (other instanceof Ring) {
            GeoShape ret = intersection((Ring) other, accuracy);
            return (ret == null) ? null : ret.simplify();
        }
        return toGeoShape().intersection(other, linearizer, accuracy);
    }
    
    public GeoShape intersection(Ring other, Tolerance accuracy) throws NullPointerException{
        return intersection(other.toArea(), accuracy);
    }

    public GeoShape intersection(final Area other, final Tolerance accuracy) throws NullPointerException {       
        if (Relation.isDisjoint(getBounds().relate(other.getBounds(), accuracy))) { // Skip networking polygonization - shortcut
            return null;
        }
        final Network network = Network.valueOf(accuracy, Linearizer.DEFAULT, this, other);
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
    public Area less(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        if (Relation.isDisjoint(getBounds().relate(other.getBounds(), accuracy))) { // Skip networking polygonization - shortcut
            return this;
        }
        if(other instanceof Ring){
            return less(((Ring)other).toArea(), accuracy);
        }else if(other instanceof Area){
            return less((Area)other, accuracy);
        }else{
            Area otherArea = other.toGeoShape(linearizer, accuracy).area;
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
        final Network network = Network.valueOf(accuracy, Linearizer.DEFAULT, this, other);
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
        
        final Network networkA = new Network();
        final Network networkB = new Network();
        final boolean[] touching = new boolean[1];
        network.forEachLink(new LinkProcessor(){
            @Override
            public boolean process(double ax, double ay, double bx, double by) {
                double x = (ax + bx) / 2;
                double y = (ay + by) / 2;
                                
                int relateA = relateInternal(x, y, accuracy);
                int relateB = other.relateInternal(x, y, accuracy);
                
                if(Relation.isDisjoint(relateA)
                    || Relation.isBInsideA(relateB)){
                    network.removeLinkInternal(ax, ay, bx, by);
                }else{
                    if(Relation.isTouch(relateA)){
                        networkA.addLink(ax, ay, bx, by);
                        if(Relation.isTouch(relateB)){
                            touching[0] = true;
                        }
                    }
                    if(Relation.isTouch(relateB)){
                        networkB.addLink(ax, ay, bx, by);
                    }
                }
                return true;
            }
        });
        
        if(!touching[0]){ // No common allLines, so make area and be done
            return Area.valueOfInternal(accuracy, network);
        }
        
        //logic here is wrong - holes from other network disappear
        List<Ring> rings = Ring.parseAllInternal(network, accuracy, true);
        for(int i = rings.size(); i-- > 0;){
            Ring ring = rings.get(i);
            int index = ring.numVects()-1;
            double bx = ring.getX(index);
            double by = ring.getY(index);
            boolean allTouchB = true;
            boolean touchA = false;
            while(index-- != 0){
                double ax = ring.getX(index);
                double ay = ring.getY(index);
                if(!networkB.hasLink(ax, ay, bx, by)){
                    allTouchB = false;
                    break;
                }
                if((!touchA) && networkA.hasLink(ax, ay, bx, by)){
                    touchA = true;
                }
                bx = ax;
                by = ay;
            }
            if(allTouchB && touchA){
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
    
    public boolean isConvexRing(){
        return (children.length == 0) && shell.isConvex();
    }
    
    public Ring getShell(){
        return shell;
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
