package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.jg.geom.Network.LinkProcessor;
import org.jg.geom.Network.VertexProcessor;
import org.jg.util.RTree;
import org.jg.util.SpatialNode;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.VectList;
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
        if (children.length == 0) {
            return shell.getBounds();
        } else {
            RectBuilder allBounds = new RectBuilder();
            if (shell != null) {
                shell.addBoundsTo(allBounds);
            }
            for (Area ringSet : children) {
                allBounds.add(ringSet.getBounds());
            }
            return allBounds.build();
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
        return GeomRelationProcessor.relate(this, geom, flatness, accuracy);
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
