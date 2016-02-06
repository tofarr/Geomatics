package org.jg;

import org.jg.SpatialNode.NodeProcessor;
import java.beans.ConstructorProperties;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;

/**
 * Simple line string. A line string should have at least 2 points and should
 * not contain duplicate sequential points.
 *
 * @author tim.ofarrell
 */
public final class LineString implements Externalizable, Cloneable {

    private final VectList vects;
    private RTree<Line> lines;
            /*
            ABOUT BUILDERS?
            Would have to write:
            VectBuilder
            LineBuilder
            RectBuilder
            
            VectList/Set/Map remain as is.
            Map and Tree cant be immutable - they reference others
                
            LineString
            Ring
            RingSet become immutable
                    
            but can be invalid.
                    
                    THINK ABOUT THIS.
            TO MAKE IMMUTABLE - NEVER RETURN LINES - SIMPLY USE ITS GET METHODS
    
            So maybe we use builder pattern for primitives only?
            Or maybe we make primitives immutable
            */

    @ConstructorProperties({"vects"})
    public LineString(VectList vects) {
        if (vects == null) {
            throw new NullPointerException("Vects must not be null");
        }
        this.vects = vects.clone();
    }

    public LineString() {
        this(new VectList());
    }

    public boolean isValid() {
        return (vects.size() > 1);
    }

    public VectList getVects(VectList target) {
        return target.addAll(vects);
    }

    public int size() {
        return vects.size() - 1;
    }

    public void normalize(Tolerance tolerance) {
        if (vects.isEmpty()) {
            return;
        }

        //Remove any duplicate points
        for (int b = vects.size() - 1, a = b - 1; a >= 0; a--, b--) {
            double ax = vects.getX(a);
            double ay = vects.getY(a);
            double bx = vects.getX(b);
            double by = vects.getY(b);
            if (tolerance.match(ax, ay, bx, by)) {
                vects.remove(b);
            }
        }

        //Remove any colinear points
        double toleranceSq = tolerance.getTolerance();
        toleranceSq *= toleranceSq;
        for (int c = vects.size() - 1, b = vects.size() - 1, a = b - 1; a >= 0; a--, b--) {
            double ax = vects.getX(a);
            double ay = vects.getY(a);
            double bx = vects.getX(b);
            double by = vects.getY(b);
            double cx = vects.getX(c);
            double cy = vects.getY(c);
            if (Line.pntSegDistSq(ax, ay, bx, by, cx, cy) <= toleranceSq) {
                vects.remove(b);
            }
        }

        //reverse if required
        int c = 0;
        int min = 0;
        int max = size() - 1;
        while ((min < max) && ((c = Vect.compare(vects.getX(min), vects.getY(min), vects.getX(max), vects.getY(max))) == 0)) {
            min++;
            max--;
        }
        if (c > 0) {
            vects.reverse();
        }
    }

    public void splitOnSelfIntersect(Tolerance tolerance, Collection<LineString> results) {
        Network network = new Network();
        network.addAllLinks(vects);
        network.explicitIntersections(tolerance);
        network.snap(tolerance);
        network.extractLines(results);
    }

    public RTree<Line> getLines() {
        if (vects.size() <= 1) {
            return new RTree<>(new Rect[0], new Line[0]);
        }
        double[] bounds = new double[(vects.size() - 1) << 2];
        Line[] lines = new Line[vects.size()-1];
        Rect rect = new Rect();
        for(int i = 1, j = 0; i < vects.size(); i++){
            int k = i-1;
            Line line = new Line(vects.getX(k), vects.getY(k), vects.getX(i), vects.getY(i));
            line.normalize();
            line.getBounds(rect);
            bounds[j++] = rect.getMinX();
            bounds[j++] = rect.getMinY();
            bounds[j++] = rect.getMaxX();
            bounds[j++] = rect.getMaxY();
            lines[k] = line;
        }
        RTree<Line> tree = new RTree<>(bounds, lines);
        return tree;
    }

    public void splitAgainst(LineString other, Tolerance tolerance, Collection<LineString> results) {
        int size = vects.size();
        if (size == 0) {
            return;
        }
        RTree<Line> lines = other.getLines();
        VectList intersections = new VectList();
        SplitAgainstProcessor processor = new SplitAgainstProcessor(lines, tolerance, intersections);
        VectList result = new VectList();
        size--;
        Rect rect = new Rect();
        Vect a = new Vect();
        Vect b = new Vect();
        for (int i = 0; i < size; i++) {
            addNonMatching(result, i);
            processor.reset(i, vects);
            processor.line.getBounds(rect);
            lines.getRoot().getInteracting(rect, processor);
            if (processor.intersections.size() > 0) {
                sortByDist(processor.line.ax, processor.line.ay, intersections, a, b);
                for (int n = 0; n < intersections.size(); n++) {
                    addNonMatching(result, i);
                    if (result.size() > 1) {
                        results.add(new LineString(result));
                        result = new VectList();
                        result.add(intersections, n);
                    }
                }
            }
        }
        if (result.size() > 1) {
            result.add(result, size);
            results.add(new LineString(result));
        }
    }

    void addNonMatching(VectList result, int index) {
        double x = vects.getX(index);
        double y = vects.getY(index);
        if (result.size() > 0) {
            int last = result.size() - 1;
            if ((result.getX(last) == x) && (result.getY(last) == y)) {
                return;
            }
        }
        result.addInternal(x, y);
    }

    void sortByDist(double originX, double originY, VectList vects, Vect a, Vect b) {
        for (int i = vects.size(); i-- > 0;) {
            vects.get(i, a);
            double dix = a.x - originX;
            double diy = a.y - originY;
            double di = (dix * dix) + (diy * diy);
            for (int j = i; j-- > 0;) {
                vects.get(j, b);
                double djx = b.x - originX;
                double djy = b.y - originY;
                double dj = (djx * djx) + (djy * djy);
                if (di > dj) {
                    di = dj;
                    vects.set(i, b);
                    vects.get(j, a);
                    Vect c = a;
                    a = b;
                    b = c;
                }
            }
        }
    }

    public double getLength() {
        return getLength(vects);
    }

    static double getLength(VectList vects) {
        double ret = 0;
        if (!vects.isEmpty()) {
            int i = vects.size() - 1;
            double bx = vects.getX(i);
            double by = vects.getY(i);
            while (--i >= 0) {
                double ax = vects.getX(i);
                double ay = vects.getY(i);
                ret = Math.sqrt(Vect.distSq(ax, ay, bx, by));
                bx = ax;
                by = ay;
            }
        }
        return ret;
    }

    @Override
    public String toString() {
        return vects.toString();
    }

    public void toString(Appendable appendable) throws IllegalStateException {
        vects.toString(appendable);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LineString) {
            LineString lineString = (LineString) obj;
            return vects.equals(lineString.vects);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + vects.hashCode();
        return hash;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        vects.writeExternal(out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        vects.readExternal(in);
    }

    static class SplitAgainstProcessor implements NodeProcessor<Line> {

        final RTree<Line> lines;
        final Line line;
        final Vect intersection;
        final VectList intersections;
        final Tolerance tolerance;

        SplitAgainstProcessor(RTree<Line> lines, Tolerance tolerance, VectList intersections) {
            this.lines = lines;
            this.line = new Line();
            this.intersection = new Vect();
            this.intersections = intersections;
            this.tolerance = tolerance;
        }

        void reset(int index, VectList vects) {
            intersections.clear();
            vects.get(index, line);
        }

        @Override
        public boolean process(SpatialNode<Line> leaf, int index) {
            Line value = leaf.getItemValue(index);
            if (line.intersectionSeg(line, tolerance, intersection)) {
                intersections.add(intersection);
            }
            return true;
        }

    }
}
