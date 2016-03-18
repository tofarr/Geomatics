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

    Area(Ring shell, Area[] children) {
        this.shell = shell;
        this.children = children;
    }

    public Area(Ring shell) {
        this(shell, NO_CHILDREN);
    }

    public static Area valueOf(Network network, Tolerance accuracy) {
        network = network.clone();
        network.explicitIntersections(accuracy);
        return valueOfInternal(network, accuracy);
    }
    
    static Area valueOfInternal(Network network, Tolerance accuracy) {
        List<Ring> rings = Ring.valueOfInternal(network, accuracy);
        switch (rings.size()) {
            case 0:
                return null;
            case 1:
                return new Area(rings.get(0), NO_CHILDREN);
            default:
                AreaBuilder builder = new AreaBuilder(null);
                for (Ring ring : rings) {
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
        Ring transformedShell = (shell == null) ? null : shell.transform(transform);
        Area[] transformedChildren = (children.length == 0) ? NO_CHILDREN : new Area[children.length];
        for (int c = 0; c < transformedChildren.length; c++) {
            transformedChildren[c] = children[c].transform(transform);
        }
        Arrays.sort(transformedChildren, COMPARATOR);
        return new Area(transformedShell, transformedChildren);
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
                    vects = rings.get(ringIndex).vects;
                    vectIndex = 0;
                    last = vects.size() - 1;
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
        return "{shell:" + shell + ", holes:" + Arrays.toString(children) + '}';
    }

    @Override
    public void toString(Appendable appendable) throws NullPointerException, GeomException {
        try {
            appendable.append('{');
            if (shell != null) {
                appendable.append("shell:");
                shell.toString(appendable);
            }
            if (children.length > 0) {
                if (shell != null) {
                    appendable.append(',');
                }
                appendable.append("children:[");
                for (int c = 0; c < children.length; c++) {
                    if (c != 0) {
                        appendable.append(',');
                    }
                    children[c].toString(appendable);
                }
                appendable.append("]}");

            }
        } catch (IOException ex) {
            throw new GeomException("Error writing", ex);
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
            return union((Area) other, accuracy);
        } else if (other instanceof Ring) {
            return union((Ring) other, accuracy);
        }
        return union(other.toGeoShape(flatness, accuracy), accuracy);
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
        network.removeWithRelation(this, accuracy, Relate.INSIDE);
        network.removeWithRelation(other, accuracy, Relate.INSIDE);
        return Area.valueOfInternal(network, accuracy);
    }

    public Area union(Ring other, Tolerance accuracy) {
        return union(other.toArea(), accuracy);
    }

    public Geom union(GeoShape other, Tolerance accuracy) throws NullPointerException {
        Area area = (other.area != null) ? union(other.area, accuracy) : this;
        if ((other.lines == null) && (other.points == null)) {
            return area;
        }
        if (other.getBounds().isDisjoint(getBounds(), accuracy)) {
            return new GeoShape(area, other.lines, other.points); // can skip mergint points and lines
        }
        MultiLineString lines;
        if (other.lines != null) {
            Network network = new Network();
            other.lines.addTo(network);
            network.explicitIntersectionsWith(getLineIndex(), accuracy);
            network.removeWithRelation(this, accuracy, Relate.INSIDE);
            lines = MultiLineString.valueOfInternal(network);
        } else {
            lines = null;
        }
        MultiPoint points = other.points;
        if (points != null) {
            VectList newPoints = new VectList();
            for (int p = 0; p < points.numPoints(); p++) {
                double x = points.getX(p);
                double y = points.getY(p);
                if (relateInternal(x, y, accuracy) == Relate.OUTSIDE) {
                    newPoints.add(x, y);
                }
            }
            if(newPoints.isEmpty()){
                points = null;
            }else if(newPoints.size() == other.points.numPoints()){
                points = other.points;
            }else{
                points = new MultiPoint(newPoints);
            }
        }
        if ((lines == null) && (points == null)) {
            return area;
        }
        return new GeoShape(area, lines, points);
    }

    @Override
    public GeoShape intersection(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        if (getBounds().isDisjoint(other.getBounds(), accuracy)) { // Skip networking polygonization - shortcut
            return null;
        }
        Network network = new Network();
        other.addTo(network, flatness, accuracy);
        network.explicitIntersectionsWith(getLineIndex(), accuracy);
        network.removeWithRelation(this, accuracy, Relate.OUTSIDE);
        return GeoShape.valueOfInternal(network, accuracy);
    }

    @Override
    public Geom less(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        if (getBounds().isDisjoint(other.getBounds(), accuracy)) { // Skip networking polygonization - shortcut
            return this;
        }
        Network network = new Network();
        addTo(network);
        other.addTo(network, flatness, accuracy);
        network.explicitIntersections(accuracy);
        network.removeWithRelation(other, accuracy, Relate.INSIDE);
        return GeoShape.valueOfInternal(network, accuracy);
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
        ret++;
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