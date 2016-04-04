package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.jg.util.RTree;
import org.jg.util.SpatialNode;
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
        List<Ring> rings = Ring.parseAllInternal(network, accuracy);
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
    public Rect getBounds() {
        if (children.length == 0) {
            return shell.getBounds();
        } else {
            RectBuilder bounds = new RectBuilder();
            if (shell != null) {
                shell.addBoundsTo(bounds);
            }
            for (Area ringSet : children) {
                bounds.add(ringSet.getBounds());
            }
            return bounds.build();
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
            Rect[] bounds = new Rect[numLines()];
            Line[] lines = new Line[bounds.length];
            addLines(bounds, lines, 0);
            RTree<Line> tree = new RTree<>(bounds, lines);
            ret = tree.getRoot();
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
        Network network = new Network();
        buffer(amt, flatness, accuracy, network);
        network.explicitIntersections(accuracy);
        return GeoShape.valueOf(network, accuracy);
    }

    void buffer(double amt, Tolerance flatness, Tolerance accuracy, Network result) {
        if (shell != null) {
            Geom geom = shell.buffer(amt, flatness, accuracy);
            if (geom != null) {
                geom.addTo(result, flatness, accuracy);
            }
            amt = -amt;
        }
        for (Area child : children) {
            child.buffer(amt, flatness, accuracy, result);
        }
    }

    @Override
    public Relate relate(Vect vect, Tolerance tolerance) throws NullPointerException {
        return relateInternal(vect.x, vect.y, tolerance);
    }

    @Override
    public Relate relate(VectBuilder vect, Tolerance tolerance) throws NullPointerException {
        return relateInternal(vect.getX(), vect.getY(), tolerance);
    }

    public Relate relate(double x, double y, Tolerance tolerance) throws IllegalArgumentException, NullPointerException {
        Vect.check(x, y);
        return relateInternal(x, y, tolerance);
    }

    Relate relateInternal(double x, double y, Tolerance tolerance) throws IllegalArgumentException, NullPointerException {
        return Ring.relateInternal(x, y, getLineIndex(), tolerance);
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
        return union(other.toGeoShape(flatness, accuracy), accuracy).simplify();
    }

    public Area union(Area other, Tolerance accuracy) {
        if (getBounds().isDisjoint(other.getBounds(), accuracy)) { // Skip networking polygonization - shortcut
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
        network.explicitIntersections(accuracy);
        VectBuilder workingVect = new VectBuilder();
        network.removeInsideOrOutsideInternal(this, accuracy, Relate.INSIDE, workingVect);
        network.removeInsideOrOutsideInternal(other, accuracy, Relate.INSIDE, workingVect);
        return Area.valueOfInternal(accuracy, network);
    }

    public Area union(Ring other, Tolerance accuracy) {
        return union(other.toArea(), accuracy);
    }

    public GeoShape union(GeoShape other, Tolerance accuracy) throws NullPointerException {
        Area area = (other.area != null) ? union(other.area, accuracy) : this;
        if ((other.lines == null) && (other.points == null)) {
            return area.toGeoShape();
        }
        if (other.getBounds().isDisjoint(getBounds(), accuracy)) {
            return new GeoShape(area, other.lines, other.points); // can skip mergint points and lines
        }
        LineSet lines = other.lines;
        if (lines != null) {
            lines = lines.less(area.toGeoShape(), accuracy);
        }
        PointSet points = other.points;
        if (points != null) {
            points = points.less(this, accuracy);
        }
        return new GeoShape(area, lines, points);
    }

    @Override
    public Geom intersection(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        if (getBounds().isDisjoint(other.getBounds(), accuracy)) { // Skip networking polygonization - shortcut
            return null;
        }
        Network network = new Network();
        addTo(network);
        other.addTo(network, flatness, accuracy);
        network.explicitIntersections(accuracy);
        VectBuilder workingVect = new VectBuilder();
        network.removeInsideOrOutsideInternal(this, accuracy, Relate.OUTSIDE, workingVect);
        network.removeInsideOrOutsideInternal(other, accuracy, Relate.OUTSIDE, workingVect);
        if(network.numVects() == 0){
            return null;
        }
        GeoShape ret = GeoShape.valueOfInternal(network, accuracy);
        return ret.simplify();
    }

    public GeoShape intersection(Area other, Tolerance accuracy) throws NullPointerException {
        if (getBounds().isDisjoint(other.getBounds(), accuracy)) { // Skip networking polygonization - shortcut
            return null;
        }
        Network network = new Network();
        addTo(network);
        other.addTo(network);
        network.explicitIntersections(accuracy);
        VectBuilder workingVect = new VectBuilder();
        network.removeInsideOrOutsideInternal(this, accuracy, Relate.OUTSIDE, workingVect);
        network.removeInsideOrOutsideInternal(other, accuracy, Relate.OUTSIDE, workingVect);
        return GeoShape.valueOfInternal(network, accuracy);
    }
    
    public Area less(final Area other, final Tolerance accuracy) throws NullPointerException {
        if (getBounds().isDisjoint(other.getBounds(), accuracy)) { // Skip networking polygonization - shortcut
            return this;
        }
        
        final VectBuilder workingVect = new VectBuilder();
        
        Network network = new Network();
        addTo(network);
        other.addTo(network);
        network.explicitIntersections(accuracy);
        network.snap(accuracy);
        
        //Add all links to ret where inside this or outside other...
        final Network ret = new Network();
        network.map.forEach(new VectMapProcessor<VectList>(){
            @Override
            public boolean process(double ax, double ay, VectList links) {
                //remove any link which touches both and is in the union
                for(int i = links.size(); i-- > 0;){
                    double bx = links.getX(i);
                    double by = links.getY(i);
                    if(Vect.compare(ax, ay, bx, by) < 0){
                        workingVect.set((ax + bx) / 2, (ay + by) / 2);
                        if((relate(workingVect, accuracy) == Relate.INSIDE)
                                || (other.relate(workingVect, accuracy) == Relate.OUTSIDE)){
                            ret.addLinkInternal(ax, ay, bx, by);
                        }
                    }
                }
                return true;
            }
        });
        
        if(ret.numVects() == 0){
            return null; // nothing outside
        }
        
        //Check for hanglines. At each hangline, follow the path touching both
        VectList vects = new VectList(ret.numVects());
        ret.map.keyList(vects);
        VectList path = null;
        for(int v = vects.size(); v-- > 0;){
            double ax = vects.getX(v);
            double ay = vects.getY(v);
            VectList links = ret.map.get(ax, ay);
            if(links.size() == 1){ // We have a hangline. Follow path joining both
               if(path == null){
                    path = new VectList();
               }else{
                    path.clear();
               }
               links = network.map.get(ax, ay); // get path from original
               for(int i = links.size(); i-- > 0;){
                   double bx = links.getX(i);
                   double by = links.getY(i);
                   workingVect.set((ax + bx) / 2, (ay + by) / 2);
                   if((relate(workingVect, accuracy) == Relate.TOUCH)
                                && (other.relate(workingVect, accuracy) == Relate.TOUCH)){
                       while(true){
                            ret.addLink(ax, ay, bx, by);
                            links = ret.map.get(bx, by);
                            if(links.size() == 2){
                                break;
                            }
                            double cx = links.getX(0);
                            double cy = links.getY(0);
                            if(cx == ax && cy == ay){
                                cx = links.getX(1);
                                cy = links.getY(1);
                            }
                            ax = bx;
                            ay = by;
                            bx = cx;
                            by = cy;
                       }
                       break;
                   }  
               }
            }
        }
        
        return Area.valueOfInternal(accuracy, ret);
    }
    
    @Override
    public Area less(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        if (getBounds().isDisjoint(other.getBounds(), accuracy)) { // Skip networking polygonization - shortcut
            return this;
        }
        if(other instanceof Ring){
            return less(((Ring)other).toArea(), accuracy);
        }else if(other instanceof Area){
            return less((Area)other, accuracy);
        }else{
            GeoShape geoShape = other.toGeoShape(flatness, accuracy);
            return (geoShape.area == null) ? this : less(geoShape.area, accuracy);
        }
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
                Relate relate = shell.relate(ring.vects.getX(i), ring.vects.getY(i), Tolerance.ZERO);
                switch (relate) {
                    case INSIDE:
                        return true;
                    case OUTSIDE:
                        return false;
                }
                i++;
            }
        }
    }

}
