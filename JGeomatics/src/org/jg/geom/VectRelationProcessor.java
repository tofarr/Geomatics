package org.jg.geom;

import org.jg.util.SpatialNode.NodeProcessor;
import org.jg.util.Tolerance;

/**
 *
 * @author tofarrell
 */
class VectRelationProcessor implements NodeProcessor<Line> {

    private final double tol;
    private double x;
    private double y;
    private int relation;
    private boolean touchesLess; // there is a line which touches the ray but does not cross it and has a lower y value
    private boolean touchesGreater; // there is a line which touches the ray but does not cross it and has a greater y value

    VectRelationProcessor(Tolerance tolerance) {
        this.tol = tolerance.tolerance;
    }

    VectRelationProcessor(Tolerance tolerance, double x, double y) {
        this(tolerance);
        reset(x, y);
    }

    public void reset(double x, double y) {
        this.x = x;
        this.y = y;
        relation = Relation.B_OUTSIDE_A;
        touchesLess = touchesGreater = false;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    int getRelation() {
        return relation;
    }

    @Override
    public boolean process(Rect bounds, Line line) {
        double ax = line.ax;
        double ay = line.ay;
        double bx = line.bx;
        double by = line.by;
        if(Vect.compare(ax, ay, bx, by) > 0){
            double tmp = ax;
            ax = bx;
            bx = tmp;
            tmp = ay;
            ay = by;
            by = tmp;
        }

        //if (Math.abs(ay - by) <= tolerance) { // line is horizontal or almost horizontal
        if (ay == by) { // line has same slope as ray
            if (Math.abs(ay - y) <= tol) { // ray is within the tolerance of line on the y axis - do they overlap on x?
                if (((ax - tol) <= x) && ((bx + tol) >= x)) {
                    relation = Relation.TOUCH;
                    return false; // no further processing is required - we have a touch
                }
            }
        } else {
            double slope = (by - ay) / (bx - ax);
            double lx = ((y - ay) / slope) + ax;  //find the x on line which crosses the ray
            if (Math.abs(lx - x) <= tol) { // if the point where line crosses ray is within the tolerated 
                relation = Relation.TOUCH;     // distance of the point, we call this a touch
                return false;
            } else if ((lx + tol) < x) { // Go from one side only...
                return true;
            }
            //We can take a shortcut here, because for all edges in a network ax <= bx...
            double minY = ay;
            double maxY = by;
            if (minY > maxY) {
                double tmp = minY;
                minY = maxY;
                maxY = tmp;
            }
            boolean intersectOnSegment = (lx >= ax) & (lx <= bx);
            if (intersectOnSegment) { // if the point of intersection is on the line segment, we have a crossing point...
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
        relation = (relation == Relation.B_OUTSIDE_A) ? Relation.B_INSIDE_A : Relation.B_OUTSIDE_A;
    }

}
