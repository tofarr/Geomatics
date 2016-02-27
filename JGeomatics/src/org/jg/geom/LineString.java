package org.jg.geom;

import java.awt.geom.PathIterator;
import org.jg.util.Network;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.VectList;

/**
 *
 * @author tofar_000
 */
public class LineString implements Geom {

    @Override
    public Rect getBounds() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addBoundsTo(RectBuilder target) throws NullPointerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Geom transform(Transform transform) throws NullPointerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PathIterator pathIterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Geom clone() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void toString(Appendable appendable) throws NullPointerException, GeomException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addTo(Network network, Tolerance tolerance) throws NullPointerException, IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
//    public double getLength() {
//        return getLength(vects);
//    }

    static double getLength(VectList vects) {
        double ret = 0;
        if (!vects.isEmpty()) {
            int i = vects.size() - 1;
            double bx = vects.getX(i);
            double by = vects.getY(i);
            while (--i >= 0) {
                double ax = vects.getX(i);
                double ay = vects.getY(i);
                ret += Math.sqrt(Vect.distSq(ax, ay, bx, by));
                bx = ax;
                by = ay;
            }
        }
        return ret;
    }

    
    
    

//    private final VectList vects;
//    private RTree<Integer> lineIndex;
//
//    @ConstructorProperties({"vects"})
//    public LineString(VectList vects) {
//        if (vects == null) {
//            throw new NullPointerException("Vects must not be null");
//        }
//        this.vects = vects.clone();
//    }
//
//    LineString(VectList vects, RTree<Integer> lineIndex) {
//        this.vects = vects;
//        this.lineIndex = lineIndex;
//    }
//
//    public boolean isValid(Tolerance tolerance) {
//        if (vects.size() < 2) {
//            return false;
//        }
//
//        //Remove any duplicate points
//        for (int b = vects.size() - 1, a = b - 1; a >= 0; a--, b--) {
//            double ax = vects.getX(a);
//            double ay = vects.getY(a);
//            double bx = vects.getX(b);
//            double by = vects.getY(b);
//            if (tolerance.match(ax, ay, bx, by)) {
//                return false;
//            }
//        }
//
//        //Remove any colinear points
//        double toleranceSq = tolerance.getTolerance();
//        toleranceSq *= toleranceSq;
//        for (int c = vects.size() - 1, b = c - 1, a = b - 1; a >= 0; a--, b--, c--) {
//            double ax = vects.getX(a);
//            double ay = vects.getY(a);
//            double bx = vects.getX(b);
//            double by = vects.getY(b);
//            double cx = vects.getX(c);
//            double cy = vects.getY(c);
//            if (Line.pntSegDistSq(ax, ay, cx, cy, bx, by) <= toleranceSq) {
//                return false;
//            }
//        }
//
//        return true;
//    }
//
//    public VectList getVects(VectList target) {
//        return target.addAll(vects);
//    }
//
//    public Vect get(int index, Vect target) {
//        return vects.get(index, target);
//    }
//
//    public Line get(int index, Line target) {
//        return vects.get(index, target);
//    }
//
//    public boolean getInteractingLines(Rect bounds, final Processor<Line> lineProcessor, final Line target) {
//        return getLineIndex().getInteracting(bounds, new NodeProcessor<Integer>() {
//            @Override
//            public boolean process(SpatialNode<Integer> leaf, int index) {
//                index = leaf.getItemValue(index);
//                vects.get(index, target);
//                return lineProcessor.process(target);
//            }
//
//        });
//    }
//
//    public boolean getOverlappingLines(Rect bounds, final Processor<Line> lineProcessor, final Line target) {
//        return getLineIndex().getOverlapping(bounds, new NodeProcessor<Integer>() {
//            @Override
//            public boolean process(SpatialNode<Integer> leaf, int index) {
//                index = leaf.getItemValue(index);
//                vects.get(index, target);
//                return lineProcessor.process(target);
//            }
//
//        });
//    }
//
//    public int size() {
//        return Math.max(vects.size() - 1, 0);
//    }
//
//    public LineString normalize(Tolerance tolerance) {
//        if (vects.isEmpty()) {
//            return this;
//        }
//
//        VectList _vects = vects.clone();
//        boolean changed = false;
//
//        //Remove any duplicate points
//        for (int b = _vects.size() - 1, a = b - 1; a >= 0; a--, b--) {
//            double ax = _vects.getX(a);
//            double ay = _vects.getY(a);
//            double bx = _vects.getX(b);
//            double by = _vects.getY(b);
//            if (tolerance.match(ax, ay, bx, by)) {
//                _vects.remove(b);
//                changed = true;
//            }
//        }
//
//        //Remove any colinear points
//        double toleranceSq = tolerance.getTolerance();
//        toleranceSq *= toleranceSq;
//        for (int c = _vects.size() - 1, b = c - 1, a = b - 1; a >= 0; a--, b--, c--) {
//            double ax = _vects.getX(a);
//            double ay = _vects.getY(a);
//            double bx = _vects.getX(b);
//            double by = _vects.getY(b);
//            double cx = _vects.getX(c);
//            double cy = _vects.getY(c);
//            if (Line.pntSegDistSq(ax, ay, cx, cy, bx, by) <= toleranceSq) {
//                _vects.remove(b);
//                changed = true;
//            }
//        }
//
//        changed |= lowestFirst(_vects); //reverse if required
//        
//        return changed ? new LineString(_vects, null) : this;
//    }
//    
//    static boolean lowestFirst(VectList vects){
//        int c = 0;
//        int min = 0;
//        int max = vects.size() - 1;
//        while ((min < max) && ((c = Vect.compare(vects.getX(min), vects.getY(min), vects.getX(max), vects.getY(max))) == 0)) {
//            min++;
//            max--;
//        }
//        if (c > 0) {
//            vects.reverse();
//            return true;
//        }
//        return false;
//    }
//
//    public Collection<LineString> splitOnSelfIntersect(Tolerance tolerance, Collection<LineString> results) {
//        Network network = new Network();
//        network.addAllLinks(vects);
//        network.explicitIntersections(tolerance);
//        network.snap(tolerance);
//        ArrayList<VectList> vectList = new ArrayList<>();
//        network.extractLines(vectList, false);
//        for(int i = vectList.size(); i-- > 0;){
//            VectList _vects = vectList.get(i);
//            lowestFirst(_vects);
//            results.add(new LineString(_vects));
//        }
//        return results;
//    }
//
//    RTree<Integer> getLineIndex() {
//        RTree<Integer> ret = lineIndex;
//        if (ret == null) {
//            ret = calculateLineIndex(vects);
//            lineIndex = ret;
//        }
//        return ret;
//    }
//
//    static RTree<Integer> calculateLineIndex(VectList vects) {
//        if (vects.size() <= 1) {
//            return new RTree<>(new Rect[0], new Integer[0]);
//        }
//        double[] bounds = new double[(vects.size() - 1) << 2];
//        Integer[] indices = new Integer[vects.size() - 1];
//        Rect rect = new Rect();
//        Line line = new Line();
//        for (int i = 1, j = 0; i < vects.size(); i++) {
//            int k = i - 1;
//            line.set(vects.getX(k), vects.getY(k), vects.getX(i), vects.getY(i));
//            line.normalize();
//            line.getBounds(rect);
//            bounds[j++] = rect.getMinX();
//            bounds[j++] = rect.getMinY();
//            bounds[j++] = rect.getMaxX();
//            bounds[j++] = rect.getMaxY();
//            indices[k] = k;
//        }
//        RTree<Integer> tree = new RTree<>(bounds, indices);
//        return tree;
//    }
//
//    public void splitAgainst(LineString other, Tolerance tolerance, Collection<LineString> results) {
//        int size = vects.size();
//        if (size == 0) {
//            return;
//        }
//        RTree<Integer> lines = other.getLineIndex();
//        VectList intersections = new VectList();
//        SplitAgainstProcessor processor = new SplitAgainstProcessor(other.vects, tolerance, intersections);
//        VectList result = new VectList();
//        size--;
//        Rect rect = new Rect();
//        Vect a = new Vect();
//        Vect b = new Vect();
//        for (int i = 0; i < size; i++) {
//            addNonMatching(result, i);
//            processor.reset(i, vects);
//            processor.a.getBounds(rect);
//            lines.getRoot().getInteracting(rect, processor);
//            if (processor.intersections.size() > 0) {
//                sortByDist(processor.a.ax, processor.a.ay, intersections, a, b);
//                for (int n = 0; n < intersections.size(); n++) {
//                    addNonMatching(result, i);
//                    if (result.size() > 1) {
//                        results.add(new LineString(result));
//                        result = new VectList();
//                        result.add(intersections, n);
//                    }
//                }
//            }
//        }
//        if (result.size() > 1) {
//            result.add(result, size);
//            results.add(new LineString(result));
//        }
//    }
//
//    void addNonMatching(VectList result, int index) {
//        double x = vects.getX(index);
//        double y = vects.getY(index);
//        if (result.size() > 0) {
//            int last = result.size() - 1;
//            if ((result.getX(last) == x) && (result.getY(last) == y)) {
//                return;
//            }
//        }
//        result.addInternal(x, y);
//    }
//
//    void sortByDist(double originX, double originY, VectList vects, Vect a, Vect b) {
//        for (int i = vects.size(); i-- > 0;) {
//            vects.get(i, a);
//            double dix = a.x - originX;
//            double diy = a.y - originY;
//            double di = (dix * dix) + (diy * diy);
//            for (int j = i; j-- > 0;) {
//                vects.get(j, b);
//                double djx = b.x - originX;
//                double djy = b.y - originY;
//                double dj = (djx * djx) + (djy * djy);
//                if (di > dj) {
//                    di = dj;
//                    vects.set(i, b);
//                    vects.get(j, a);
//                    Vect c = a;
//                    a = b;
//                    b = c;
//                }
//            }
//        }
//    }
//
//    @Override
//    public String toString() {
//        return vects.toString();
//    }
//
//    public void toString(Appendable appendable) throws IllegalStateException {
//        vects.toString(appendable);
//    }
//
//    @Override
//    protected Object clone() throws CloneNotSupportedException {
//        return super.clone(); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (obj instanceof LineString) {
//            LineString lineString = (LineString) obj;
//            return vects.equals(lineString.vects);
//        }
//        return false;
//    }
//
//    @Override
//    public int hashCode() {
//        int hash = 5;
//        hash = 89 * hash + vects.hashCode();
//        return hash;
//    }
//
//    @Override
//    public void writeExternal(ObjectOutput out) throws IOException {
//        vects.writeExternal(out);
//    }
//
//    @Override
//    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
//        vects.readExternal(in);
//    }
//
//    static class SplitAgainstProcessor implements NodeProcessor<Integer> {
//
//        final VectList vects;
//        final Line a;
//        final Line b;
//        final Vect intersection;
//        final VectList intersections;
//        final Tolerance tolerance;
//
//        SplitAgainstProcessor(VectList vects, Tolerance tolerance, VectList intersections) {
//            this.vects = vects;
//            this.a = new Line();
//            this.b = new Line();
//            this.intersection = new Vect();
//            this.intersections = intersections;
//            this.tolerance = tolerance;
//        }
//
//        void reset(int index, VectList vects) {
//            intersections.clear();
//            vects.get(index, a);
//        }
//
//        @Override
//        public boolean process(SpatialNode<Integer> leaf, int index) {
//            index = leaf.getItemValue(index);
//            vects.get(index, b);
//            if (a.intersectionSeg(b, tolerance, intersection)) {
//                intersections.add(intersection);
//            }
//            return true;
//        }
//
//    }

    @Override
    public Geom buffer(double amt, Tolerance tolerance) throws IllegalArgumentException, NullPointerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
