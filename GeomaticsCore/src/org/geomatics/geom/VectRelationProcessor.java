package org.geomatics.geom;

import org.geomatics.util.SpatialNode.NodeProcessor;
import org.geomatics.util.Tolerance;

/**
 *
 * @author tofarrell
 */
class VectRelationProcessor implements NodeProcessor<Line> {

    private final Tolerance tolerance;
    private double x;
    private double y;
    private int relation;
    private boolean touchesLess; // there is a line which touches the ray but does not cross it and has a lower y value
    private boolean touchesGreater; // there is a line which touches the ray but does not cross it and has a greater y value

    VectRelationProcessor(Tolerance tolerance) {
        this.tolerance = tolerance;
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
        
        if(Line.distSegVectSq(line.ax, line.ay, line.bx, line.by, x, y) <= tolerance.toleranceSq){
            relation = Relation.TOUCH;
            return false;
        }
        
        if(tolerance.match(line.ay, line.by)){ // line is almost parallell to ray
            return true; // skip and continue.
        }
        
        //Get progress along line
        double u = (y - line.ay) / (line.by - line.ay);
        double ix = u * (line.bx - line.ax) + line.ax;
        if(ix >= x){
            if(tolerance.match(ix, y, line.ax, line.ay)){
                touchEnd(line.ay, line.by);
            }else if(tolerance.match(ix, y, line.bx, line.by)){
                touchEnd(line.by, line.ay);
            }else { // if((u > 0) && (u < 1)){
                flipRelation();
            }
        }
        return true;
    }
     
    private void touchEnd(double touchY, double otherY){
        if(touchY < otherY){
            if (touchesLess) { // we have a crossing point
                touchesLess = false;
                flipRelation();
            } else {
                touchesGreater = !touchesGreater;
            }
        }else{
            if (touchesGreater) { // we have a crossing point
                touchesGreater = false;
                flipRelation();
            } else {
                touchesLess = !touchesLess;
            }
        }
    }
        

    private void flipRelation() {
        relation = (relation == Relation.B_OUTSIDE_A) ? Relation.B_INSIDE_A : Relation.B_OUTSIDE_A;
    }

}
