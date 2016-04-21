
package org.jg.geom;

import org.jg.util.SpatialNode.NodeProcessor;
import org.jg.util.Tolerance;
import org.jg.util.VectList;
import org.jg.util.VectSet;

/**
 *
 * @author tofarrell
 */
class IntersectionProcessor implements NodeProcessor<Line> {
    
    private final Tolerance accuracy;
    private final VectSet intersections;
    private final VectBuilder intersection;
    private Line a;

    public IntersectionProcessor(Tolerance accuracy) {
        this.accuracy = accuracy;
        this.intersections = new VectSet();
        this.intersection = new VectBuilder();
    }
    
    public void reset(Line a){
        this.a = a;
        intersections.clear();
    }

    public VectList getIntersections(VectList result) {
        result.clear();
        intersections.toList(result);
        result.sortByDist(a.ax, a.ay);
        return result;
    }

    @Override
    public boolean process(Rect bounds, Line b) {
        if(a.intersectionSeg(b, accuracy, intersection)){
            intersections.add(intersection);
        }
        return true;
    }
}
