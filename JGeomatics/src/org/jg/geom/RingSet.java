package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
public class RingSet implements Geom {

    public static final RingSet[] EMPTY = new RingSet[0];

    final Ring shell;
    final RingSet[] children;
    SpatialNode<Line> lineIndex;

    RingSet(Ring shell, RingSet[] children) {
        this.shell = shell;
        this.children = children;
    }

    public RingSet(Ring shell, Collection<RingSet> children) {
        this.shell = shell;
        this.children = ((children == null) || children.isEmpty()) ? EMPTY : children.toArray(new RingSet[children.size()]);
        if ((shell == null) && children.isEmpty()) {
            throw new IllegalArgumentException("Must define either an outer shell or children");
        }
    }

    public RingSet(Ring shell) {
        this(shell, EMPTY);
    }

    public static RingSet valueOf(Network network) {
        List<Ring> rings = Ring.valueOf(network);
        switch (rings.size()) {
            case 0:
                return null;
            case 1:
                return new RingSet(rings.get(0), EMPTY);
            default:
                RingSetBuilder builder = new RingSetBuilder(null);
                for (Ring ring : rings) {
                    builder.add(ring);
                }
                return builder.build();
        }
    }

    public double getArea() {
        if (shell == null) {
            double ret = 0;
            for (RingSet ringSet : children) {
                ret += ringSet.getArea();
            }
            return ret;
        } else {
            double ret = shell.getArea();
            for (RingSet ringSet : children) {
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
            for (RingSet ringSet : children) {
                ringSet.addBoundsTo(bounds);
            }
            return bounds.build();
        }
    }

    @Override
    public void addBoundsTo(RectBuilder target) throws NullPointerException {
        target.add(getBounds());
    }

    @Override
    public RingSet transform(Transform transform) throws NullPointerException {
        Ring transformedShell = (shell == null) ? null : shell.transform(transform);
        RingSet[] transformedChildren = (children.length == 0) ? EMPTY : new RingSet[children.length];
        for(int c = 0; c < transformedChildren.length; c++){
            transformedChildren[c] = children[c].transform(transform);
        }
        return new RingSet(transformedShell, transformedChildren);
    }

    @Override
    public PathIterator pathIterator() {
        return new PathIterator(){
            
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
                if(vectIndex == last){
                    ringIndex++;
                    vects = rings.get(ringIndex).vects;
                    vectIndex = 0;
                    last = vects.size() - 1;
                }else{
                    vectIndex++;
                }
            }

            @Override
            public int currentSegment(float[] coords) {
                coords[0] = (float)vects.getX(vectIndex);
                coords[1] = (float)vects.getY(vectIndex);
                if(vectIndex == 0){
                    return SEG_MOVETO;
                }else if(vectIndex == last){
                    return SEG_CLOSE;
                }else{
                    return SEG_LINETO;
                }
            }

            @Override
            public int currentSegment(double[] coords) {
                coords[0] = vects.getX(vectIndex);
                coords[1] = vects.getY(vectIndex);
                if(vectIndex == 0){
                    return SEG_MOVETO;
                }else if(vectIndex == last){
                    return SEG_CLOSE;
                }else{
                    return SEG_LINETO;
                }
            }
        };
    }

    @Override
    public RingSet clone() {
        return this;
    }

    public int numRings() {
        int ret = (shell == null) ? 0 : 1;
        for (RingSet child : children) {
            ret += child.numRings();
        }
        return ret;
    }

    public int numLines() {
        int ret = 0;
        if (shell != null) {
            ret += shell.numLines();
        }
        for (RingSet child : children) {
            ret += child.numLines();
        }
        return ret;
    }

    public int numVects() {
        int ret = 0;
        if (shell != null) {
            ret += shell.numVects();
        }
        for (RingSet child : children) {
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
        for (RingSet child : children) {
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
        for (RingSet child : children) {
            child.getRings(result);
        }
    }
    
    public VectSet getVects(VectSet result){
        if (shell != null) {
            result.addAll(shell.vects);
        }
        for (RingSet child : children) {
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

    @Override
    public void addTo(Network network, Tolerance tolerance) throws NullPointerException, IllegalArgumentException {
        if (shell != null) {
            shell.addTo(network, tolerance);
        }
        for (int c = 0; c < children.length; c++) {
            children[c].addTo(network, tolerance);
        }
    }

    @Override
    public Geom buffer(double amt, Tolerance flatness, Tolerance tolerance) throws IllegalArgumentException, NullPointerException {
        Network network = new Network();
        buffer(amt, flatness, tolerance, network);
        return RingSet.valueOf(network);
    }

    void buffer(double amt, Tolerance flatness, Tolerance tolerance, Network result) {
        if (shell != null) {
            RingSet ringSet = shell.buffer(amt, flatness, tolerance);
            if (ringSet != null) {
                ringSet.addTo(result, tolerance);
            }
            amt = -amt;
        }
        for (RingSet child : children) {
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

    public RingSet intersection(RingSet other, Tolerance tolerance) {
        Network network = new Network();
        addTo(network, tolerance);
        other.addTo(network, tolerance);
        network.explicitIntersections(tolerance);
        network.removeOutsideInternal(this, tolerance);
        network.removeOutsideInternal(other, tolerance);
        return valueOf(network);
    }

    public RingSet union(RingSet other, Tolerance tolerance) {
        Network network = new Network();
        addTo(network, tolerance);
        other.addTo(network, tolerance);
        network.explicitIntersections(tolerance);
        network.removeInsideInternal(this, tolerance);
        network.removeInsideInternal(other, tolerance);
        return valueOf(network);
    }

    public RingSet less(RingSet other, Tolerance tolerance) {
        Network network = new Network();
        addTo(network, tolerance);
        other.addTo(network, tolerance);
        network.explicitIntersections(tolerance);
        network.removeOutsideInternal(this, tolerance);
        network.removeInsideInternal(other, tolerance);
        return valueOf(network);
    }

    public static RingSet intersection(Tolerance tolerance, RingSet... ringSets) {
        Network network = new Network();
        for (RingSet ringSet : ringSets) {
            ringSet.addTo(network, tolerance);
        }
        network.explicitIntersections(tolerance);
        for (RingSet ringSet : ringSets) {
            network.removeOutsideInternal(ringSet, tolerance);
        }
        return valueOf(network);
    }

    public static RingSet union(Tolerance tolerance, RingSet... ringSets) {
        Network network = new Network();
        for (RingSet ringSet : ringSets) {
            ringSet.addTo(network, tolerance);
        }
        network.explicitIntersections(tolerance);
        for (RingSet ringSet : ringSets) {
            network.removeInsideInternal(ringSet, tolerance);
        }
        return valueOf(network);
    }

    static class RingSetBuilder {

        final Ring shell;
        final ArrayList<RingSetBuilder> children;

        RingSetBuilder(Ring shell) {
            this.shell = shell;
            children = new ArrayList<>();
        }

        RingSet build() {
            if (shell == null) {
                if (children.size() == 1) {
                    return children.get(0).build();
                }
            }
            RingSet[] _children = new RingSet[children.size()];
            for (int c = 0; c < _children.length; c++) {
                _children[c] = children.get(c).build();
            }
            return new RingSet(shell, _children);
        }

        boolean add(Ring ring) {
            if (!canAdd(ring)) {
                return false;
            }
            for (RingSetBuilder child : children) {
                if (child.add(ring)) {
                    return true;
                }
            }
            children.add(new RingSetBuilder(ring));
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
