package org.jg;

import org.jg.SpatialNode.NodeProcessor;
import java.beans.Transient;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author tofar_000
 */
public class Ring implements Externalizable, Cloneable {

    final VectList vects;
    private RTree<Line> lines;

    public Ring(VectList vects) {
        int last = vects.size() - 1;
        if (last < 3) {
            throw new IllegalArgumentException("Rings must have at least 4 points");
        }
        if ((vects.getX(0) != vects.getX(last)) || (vects.getY(0) != vects.getY(last))) {
            throw new IllegalArgumentException("Rings must be closed!");
        }
        this.vects = vects;
    }

    /*public Rect getBounds(Rect bounds){
     return vects.getBounds(bounds);
     }*/
    public double getArea() {
        return getArea(vects);
    }

    static double getArea(VectList vects) throws NullPointerException, IndexOutOfBoundsException {
        int index = vects.size();
        Vect b = vects.get(--index, new Vect());
        Vect a = new Vect();
        double area = 0;
        while (index > 0) {
            vects.get(--index, a);
            area += (b.x * a.y) - (a.x * b.y);
            Vect c = a;
            a = b;
            b = c;
        }
        area /= 2;
        return area;
    }

    public double getLength() {
        return LineString.getLength(vects);
    }

    static VectList rotateMinFirst(VectList segment, VectList target) {
        int offset = 0;
        Vect min = segment.get(offset, new Vect());
        Vect vect = new Vect();
        int numVertices = segment.size();
        for (int v = numVertices - 1; v-- > 1;) {
            segment.get(v, vect);
            if (min.compareTo(vect) > 0) {
                offset = v;
                min = vect;
            }
        }
        if (offset != 0) {
            target.clear();
            numVertices--;
            for (long v = 0; v < numVertices; v++) {
                target.add(segment, offset);
                offset++;
                if (offset == numVertices) {
                    offset = 0;
                }
            }
            target.add(target, 0);
        }
        return target;
    }

    public Vect getCentroid(Vect target) {
        int index = vects.size();
        Vect b = vects.get(--index, new Vect());
        double twiceArea = 0;
        double x = 0;
        double y = 0;
        Vect a = new Vect();
        while (index > 0) {
            vects.get(--index, a);
            double f = (b.x * a.y) - (a.x * b.y);
            twiceArea += f;
            x += (a.x + b.x) * f;
            y += (a.y + b.y) * f;
            b = a;
        }
        double f = twiceArea * 3;
        return a.set(x / f, y / f);
    }

    /**
     * Determine if the ring is convex.
     *
     * @return true if the ring was convex, false otherwise
     */
    @Transient
    public boolean isConvex() {
        int index = nextConcaveAngle(vects, vects.size()); // Because of the way it is sorted (minX,minY), the angle between (max,0,1) is guaranteed to be convex and can be ignored
        return (index < 0);
    }

    static int nextConcaveAngle(VectList edges, int fromIndex) {
        Vect c = edges.get(--fromIndex, new Vect()); // Because of the way it is sorted (minX,minY), the angle between (max,0,1) is guaranteed to be convex and can be ignored
        Vect b = edges.get(--fromIndex, new Vect());
        Vect a = new Vect();
        while (fromIndex > 0) {
            edges.get(--fromIndex, a);
            if (Line.sign(a.x, a.y, c.x, c.y, b.x, b.y) > 0) {
                return (fromIndex + 1);
            }
            c = b;
            b = a;
        }
        return -1;
    }

    /**
     * Determine if this Ring Network contains the point given
     *
     * @param pnt
     * @return true if it contains the point, false otherwise
     * @throws NullPointerException if pnt was null
     */
    public Relate relate(Vect pnt, Tolerance tolerance) throws NullPointerException {
        return relateInternal(pnt.x, pnt.y, tolerance);
    }

    /**
     * Determine if this Ring Network contains the point given
     *
     * @param pnt
     * @param tolerance
     * @return true if it contains the point, false otherwise
     * @throws NullPointerException if pnt was null
     */
    public Relate relate(double x, double y, Tolerance tolerance) throws NullPointerException {
        return relateInternal(x, y, tolerance);
    }

    Relate relateInternal(double x, double y, Tolerance tolerance) throws IllegalArgumentException {
        Rect bounds = vects.getBounds(new Rect());
        double tol = tolerance.tolerance;
        if (((x + tol) < bounds.minX)
                || // if the point is not within the tolerance distance
                ((x - tol) > bounds.maxX)
                || // of the bounding box, it cannot be considered inside
                ((y + tol) < bounds.minY)
                || // or touching the network
                ((y - tol) > bounds.maxY)) {
            return Relate.OUTSIDE;
        }

        double padding = tol * 2;
        bounds.set(x - padding, y - padding, bounds.maxX + padding, y + padding); // Generate a selection ray from the point off to the right including the tolerance

        RelationVisitor visitor = new RelationVisitor(tolerance, x, y);
        getLines().root.getInteracting(bounds, visitor);
        return visitor.relation;
    }

    public VectList getVects(VectList target) {
        return target.addAll(vects);
    }

    public RTree<Line> getLines(RTree<Line> target) {
        return target.addAll(getLines());
    }

    RTree<Line> getLines() {
        if (lines == null) {
            RTree<Line> tree = new RTree<>();
            for (int i = vects.size() - 1; i-- > 0;) {
                Line line = vects.get(i, new Line());
                line.normalize();
                tree.add(line.getBounds(new Rect()), line);
            }
            lines = tree;
        }
        return lines;

    }

    public boolean intersects(Ring ring, final Tolerance tolerance) throws IllegalArgumentException {
        Rect bounds = vects.getBounds(new Rect());
        Rect selectBounds = ring.vects.getBounds(new Rect());
        if (!bounds.isOverlapping(selectBounds)) {
            return false;
        }

        double padding = tolerance.getTolerance() * 2;

        //Go through this, testing each point to determine if any are inside ring
        final RelationVisitor relationVisitor = new RelationVisitor(tolerance);
        Vect vect = new Vect();
        RTree<Line> _lines = getLines();

        VectList ringVects = ring.vects;
        for (int i = ringVects.size() - 1; i-- > 0;) {
            double x = ringVects.getX(i);
            double y = ringVects.getY(i);
            relationVisitor.reset(x, y);
            selectBounds.set(x - padding, y - padding, bounds.maxX + padding, y + padding); // Generate a selection ray from the point off to the right including the tolerance    
            _lines.root.getInteracting(selectBounds, relationVisitor);
            if (relationVisitor.relation == Relate.INSIDE) {
                return true;
            }
        }

        final NumIntersectionVisitor numIntersectionVisitor = new NumIntersectionVisitor(tolerance);
        Line line = new Line();
        for (int i = ringVects.size() - 1; i-- > 0;) {
            ringVects.get(i, line);
            line.getBounds(selectBounds);
            numIntersectionVisitor.reset(line);
            _lines.root.getInteracting(selectBounds, numIntersectionVisitor);
            if (numIntersectionVisitor.getNumIntersection() > 1) {
                return true;
            }
        }

        return false;
    }

    public boolean contains(Ring ring, final Tolerance tolerance) throws IllegalArgumentException {
        Rect bounds = vects.getBounds(new Rect());
        Rect selectBounds = ring.vects.getBounds(new Rect());
        if (!bounds.contains(selectBounds)) {
            return false;
        }

        double padding = tolerance.getTolerance() * 2;

        //Go through this, testing each point to determine if any are inside ring
        final RelationVisitor relationVisitor = new RelationVisitor(tolerance);
        Vect vect = new Vect();
        RTree<Line> _lines = getLines();

        VectList ringVects = ring.vects;
        for (int i = ringVects.size() - 1; i-- > 0;) {
            double x = ringVects.getX(i);
            double y = ringVects.getY(i);
            relationVisitor.reset(x, y);
            selectBounds.set(x - padding, y - padding, bounds.maxX + padding, y + padding); // Generate a selection ray from the point off to the right including the tolerance    
            _lines.root.getInteracting(selectBounds, relationVisitor);
            if (relationVisitor.relation == Relate.OUTSIDE) {
                return true;
            }
        }

        final NumIntersectionVisitor numIntersectionVisitor = new NumIntersectionVisitor(tolerance);
        Line line = new Line();
        for (int i = ringVects.size() - 1; i-- > 0;) {
            ringVects.get(i, line);
            line.getBounds(selectBounds);
            numIntersectionVisitor.reset(line);
            _lines.root.getInteracting(selectBounds, numIntersectionVisitor);
            if (numIntersectionVisitor.getNumIntersection() > 1) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + vects.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Ring) && vects.equals(((Ring) obj).vects);
    }

    @Override
    public String toString() {
        return "{Ring:" + vects.toString() + "}";
    }

    public void toString(Appendable appendable) throws IOException {
        appendable.append("{Ring:");
        vects.toString(appendable);
        appendable.append("}");
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    static class RelationVisitor implements NodeProcessor<Line> {

        final Tolerance tolerance;
        final Vect i;
        final Vect j;
        double x;
        double y;
        Relate relation;
        boolean touchesLess; // there is a line which touches the ray but does not cross it and has a lower y value
        boolean touchesGreater; // there is a line which touches the ray but does not cross it and has a greater y value
        

        RelationVisitor(Tolerance tolerance, double x, double y) {
            this.tolerance = tolerance;
            this.x = x;
            this.y = y;
            this.relation = Relate.OUTSIDE;
            this.i = new Vect();
            this.j = new Vect();
        }

        RelationVisitor(Tolerance tolerance) {
            this.tolerance = tolerance;
            this.relation = Relate.OUTSIDE;
            this.i = new Vect();
            this.j = new Vect();
        }

        void reset(double x, double y) {
            this.x = x;
            this.y = y;
            this.relation = Relate.OUTSIDE;
            touchesLess = touchesGreater = false;
        }

        @Override
        public boolean process(SpatialNode<Line> leaf, int index) {
            Line link = leaf.getItemValue(index);
            link.getA(i);
            link.getB(j);
            if (i.y == j.y) { // line has same slope as ray
                if (tolerance.match(i.y, y)) { // ray is within the tolerance of line on the y axis - do they overlap on x?
                    if (((i.x - tolerance.tolerance) <= x) && ((j.x + tolerance.tolerance) >= x)) {
                        relation = Relate.TOUCH;
                        return false; // no further processing is required - we have a touch
                    }
                }
            } else {
                double slope = (j.y - i.y) / (j.x - i.x);
                double lx = ((y - i.y) / slope) + i.x;  //find the x on line which crosses the ray
                if (tolerance.match(lx, x)) { // if the point where line crosses ray is within the tolerated 
                    relation = Relate.TOUCH;     // distance of the point, we call this a touch
                    return false;
                } else if ((lx + tolerance.tolerance) < x) { // Go from one side only...
                    return true;
                }
                //We can take a shortcut here, because for all edges in a network ax <= bx...
                double minY = i.y;
                double maxY = j.y;
                if (minY > maxY) {
                    double tmp = minY;
                    minY = maxY;
                    maxY = tmp;
                }
                if ((lx >= i.x) && (lx <= j.x)) { // if the point of intersection is on the line segment, we have a crossing point...
                    if (minY == y) {
                        if (touchesLess) { // we have a crossing point
                            touchesLess = false;
                            flipRelation();
                        } else {
                            touchesGreater = !touchesGreater;
                        }
                    } else if (maxY == y) {
                        if (touchesGreater) { // we have a crossing point
                            touchesGreater = false;
                            flipRelation();
                        } else {
                            touchesLess = !touchesLess;
                        }
                    } else {
                        flipRelation();
                    }
                }
            }

            return true; // continue processing
        }

        private void flipRelation() {
            relation = (relation == Relate.OUTSIDE) ? Relate.INSIDE : Relate.OUTSIDE;
        }

    }
    
    static class NumIntersectionVisitor implements NodeProcessor<Line> {

        final Tolerance tolerance;

        NumIntersectionVisitor(Tolerance tolerance) {
            this.tolerance = tolerance;
        }
        
        
        @Override
        public boolean process(SpatialNode<Line> leaf, int index) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        private void reset(Line line) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        private int getNumIntersection() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
}
