package org.jg.algorithm;

import org.jg.geom.Area;
import org.jg.geom.Geom;
import org.jg.geom.Linearizer;
import org.jg.geom.Rect;
import org.jg.geom.Ring;
import org.jg.geom.Vect;
import org.jg.util.Tolerance;

/**
 *
 * @author tofarrell
 */
public class PoleOfInaccessibility {
    
    private final Linearizer linearizer;
    private final Tolerance accuracy;

    public PoleOfInaccessibility(Linearizer linearizer, Tolerance accuracy) {
        this.linearizer = linearizer;
        this.accuracy = accuracy;
    }

    public Vect getPoleOfInAccessibility(Area area) {
        double generalizeFactor = getInitialBufferFactor(area) / 4;
        while (!area.isConvexRing()) {

            double bufferFactor = getInitialBufferFactor(area); //Choose an initial buffer factor

            Geom buffered = area.buffer(bufferFactor, linearizer, accuracy);
            while(buffered == null){ // if area disappeared, reduce by half and try again
                bufferFactor /= 2;
                buffered = area.buffer(bufferFactor, linearizer, accuracy);
            }
            
            //area = area.generalize(); // douglas peucker by preset amount - this helps to remove stubborn dents
        }
        return area.getShell().getCentroid();
    }

    /**
     * Get the initial buffer factor - this may result in the shape being buffered out of existence in some cases,
     * in which case a less extreme value will be required
     * @param area
     * @return
     */
    public double getInitialBufferFactor(Area area) {
        Ring shell = area.getShell();
        if (shell != null) { // Area has an outer shell, so get the approximate buffer factor from it
            double ret = getBufferFactor(shell.getBounds());
            return ret;
        }
        //Area has no outer shell, so get the approximate buffer factor from the largest ring
        double ret = 0;
        for(int i = area.numChildren(); i-- > 0;){
            Area child = area.getChild(i);
            double factor = getBufferFactor(child.getShell().getBounds());
            ret = Math.min(ret, factor); // Use min because factors are less than 0
        }
        return ret;
    }

    private double getBufferFactor(Rect bounds) {
        double side = Math.min(bounds.getWidth(), bounds.getHeight());
        double ret = -side / 3;
        return ret;
    }
}
