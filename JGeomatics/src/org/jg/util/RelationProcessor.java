package org.jg.util;

import org.jg.geom.Line;
import org.jg.geom.Rect;
import org.jg.geom.Relate;
import org.jg.util.SpatialNode.NodeProcessor;

/**
 *
 * @author tofarrell
 */
public class RelationProcessor implements NodeProcessor<Line> {

    final double tol;
    double x;
    double y;
    Relate relate;
    boolean touchesLess; // there is a line which touches the ray but does not cross it and has a lower y value
    boolean touchesGreater; // there is a line which touches the ray but does not cross it and has a greater y value

    public RelationProcessor(Tolerance tolerance) {
        this.tol = tolerance.tolerance;
    }

    public RelationProcessor(Tolerance tolerance, double x, double y) {
        this(tolerance);
        this.x = x;
        this.y = y;
        relate = Relate.OUTSIDE;
    }

    public void reset(double x, double y) {
        this.x = x;
        this.y = y;
        relate = Relate.OUTSIDE;
        touchesLess = touchesGreater = false;
    }

    public Relate getRelate() {
        return relate;
    }

    @Override
    public boolean process(Rect bounds, Line line) {
        double ax = line.ax;
        double ay = line.ay;
        double bx = line.bx;
        double by = line.by;

        //if (Math.abs(ay - by) <= tolerance) { // line is horizontal or almost horizontal
        if (ay == by) { // line has same slope as ray
            if (Math.abs(ay - y) <= tol) { // ray is within the tolerance of line on the y axis - do they overlap on x?
                if (((ax - tol) <= x) && ((bx + tol) >= x)) {
                    relate = Relate.TOUCH;
                    return false; // no further processing is required - we have a touch
                }
            }
        } else {
            double slope = (by - ay) / (bx - ax);
            double lx = ((y - ay) / slope) + ax;  //find the x on line which crosses the ray
            if (Math.abs(lx - x) <= tol) { // if the point where line crosses ray is within the tolerated 
                relate = Relate.TOUCH;     // distance of the point, we call this a touch
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
            if ((lx >= ax) && (lx <= bx)) { // if the point of intersection is on the line segment, we have a crossing point...
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
        relate = (relate == Relate.OUTSIDE) ? Relate.INSIDE : Relate.OUTSIDE;
    }

}
