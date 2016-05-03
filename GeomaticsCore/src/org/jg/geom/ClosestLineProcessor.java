package org.jg.geom;

import org.jg.util.SpatialNode.NodeProcessor;

/**
 *
 * @author tofarrell
 */
final class ClosestLineProcessor implements NodeProcessor<Line> {

    private Vect vect;
    private double minDistSq;
    private Line minLine;

    public ClosestLineProcessor() {
    }

    public ClosestLineProcessor(Vect vect) {
        reset(vect);
    }
    
    public void reset(Vect vect){
        this.vect = vect;
        minDistSq = Double.MAX_VALUE;
        minLine = null;
    }
    
    @Override
    public boolean process(Rect bounds, Line line) {
        double distSq = line.distSegVectSq(vect);
        if(distSq < minDistSq){
            minDistSq = distSq;
            minLine = line;
        }
        return true;
    }

    public double getMinDistSq() {
        return minDistSq;
    }

    public Line getMinLine() {
        return minLine;
    }
}
