package org.jg.geom;

import org.jg.geom.Line;
import org.jg.geom.Rect;
import org.jg.geom.Vect;
import org.jg.geom.VectBuilder;
import org.jg.util.SpatialNode.NodeProcessor;
import org.jg.util.Tolerance;
import org.jg.util.VectList;

/**
 *
 * @author tofar
 */
class IntersectionFinder implements NodeProcessor<Line> {

    final Tolerance accuracy;
    final VectList intersections;
    final VectBuilder workingVect;
    Line i;

    IntersectionFinder(Tolerance accuracy) {
        this.accuracy = accuracy;
        this.intersections = new VectList();
        this.workingVect = new VectBuilder();
    }

    void reset(Line i) {
        this.i = i;
        intersections.clear();
    }
    
    VectList getIntersection(){
        return intersections;
    }

    @Override
    public boolean process(Rect bounds, Line j) {
        if ((!i.equals(j)) && i.intersectionSeg(j, accuracy, workingVect)) {
                if ((Vect.compare(i.ax, i.ay, workingVect.getX(), workingVect.getY()) != 0)
                        && (Vect.compare(i.bx, i.by, workingVect.getX(), workingVect.getY()) != 0)) {
                    intersections.add(workingVect);
                }
            }
            return true;
    }

}
