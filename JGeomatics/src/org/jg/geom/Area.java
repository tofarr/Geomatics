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
import org.jg.util.VectSet;

/**
 *
 * @author tofar_000
 */
public class Area implements Geom {

    public static final Area[] EMPTY = new Area[0];

    final Ring shell;
    final Area[] children;
    SpatialNode<Line> lineIndex;
    Boolean valid;

    Area(Ring shell, Area[] children) {
        this.shell = shell;
        this.children = children;
    }

    public Area(Ring shell, Collection<Area> children) {
        this.shell = shell;
        this.children = ((children == null) || children.isEmpty()) ? EMPTY : children.toArray(new Area[children.size()]);
        if ((shell == null) && children.isEmpty()) {
            throw new IllegalArgumentException("Must define either an outer shell or children");
        }
    }

    public Area(Ring shell) {
        this(shell, EMPTY);
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
        Area[] transformedChildren = (children.length == 0) ? EMPTY : new Area[children.length];
        for (int c = 0; c < transformedChildren.length; c++) {
            transformedChildren[c] = children[c].transform(transform);
        }
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
    public GeoShape toGeoShape(Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        return new GeoShape(this, GeoShape.NO_LINES, GeoShape.NO_POINTS);
    }

    @Override
    public Geom buffer(double amt, Tolerance flatness, Tolerance accuracy) throws IllegalArgumentException, NullPointerException {
        Network network = new Network();
        buffer(amt, flatness, accuracy, network);
        network.explicitIntersections(accuracy);
        return GeoShape.consumeNetwork(network, accuracy);
    }

    void buffer(double amt, Tolerance flatness, Tolerance tolerance, Network result) {
        if (shell != null) {
            
            Area ringSet = shell.buffer(amt, flatness, tolerance);
            if (ringSet != null) {
                ringSet.addTo(result);
            }
            amt = -amt;
        }
        for (Area child : children) {
            child.buffer(amt, flatness, tolerance, result);
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

    public Relate Relate(double x, double y, Tolerance tolerance) throws IllegalArgumentException, NullPointerException {
        Vect.check(x, y);
        return relateInternal(x, y, tolerance);
    }

    Relate relateInternal(double x, double y, Tolerance tolerance) throws IllegalArgumentException, NullPointerException {
        return Ring.relateInternal(x, y, getLineIndex(), tolerance);
    }

    @Override
    public Geom union(Geom other, Tolerance flatness, Tolerance tolerance) throws NullPointerException {
        return Network.union(flatness, tolerance, this, other);
    }

    public Geom union(GeoShape other, Tolerance accuracy) throws NullPointerException {
        
    }
    
    public Area union(Tolerance flatness, Tolerance tolerance) throws NullPointerException {
        if (valid) {
            return this;
        }
        return (Area) Network.union(flatness, tolerance, this);
    }

    @Override
    public Geom intersection(Geom other, Tolerance flatness, Tolerance tolerance) throws NullPointerException {
        if (getBounds().buffer(tolerance.tolerance).isDisjoint(other.getBounds())) {
            return null;
        } else {
            return Network.intersection(flatness, tolerance, this, other);
        }
    }
    
    public Geom intersection(GeoShape other, Tolerance accuracy) throws NullPointerException {
        
    }
    
    
    @Override
    public Geom less(Geom other, Tolerance flatness, Tolerance tolerance) throws NullPointerException {
        if (getBounds().isDisjoint(other.getBounds())) {
            return this;
        }
        return Network.less(flatness, tolerance, this, other);
    }

    public Geom less(GeoShape other, Tolerance accuracy) throws NullPointerException {
        
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

}
