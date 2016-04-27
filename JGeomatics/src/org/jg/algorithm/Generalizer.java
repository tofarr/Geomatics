
package org.jg.algorithm;

import java.util.ArrayList;
import java.util.List;
import org.jg.geom.Line;
import org.jg.geom.Network;
import org.jg.util.Tolerance;
import org.jg.util.VectList;

/**
 * Implementation of the Dougles Peucker algorithm. For a constrained version, use ConstrainedDouglasPeucker
 * @author tofarrell
 */
public class Generalizer {
    
    private final Tolerance tolerance;

    public Generalizer(Tolerance tolerance) {
        this.tolerance = tolerance;
    }
    
    public VectList generalize(VectList input){
        VectList output = new VectList();
        generalize(input, output);
        return output;
    }
    
    public void generalize(VectList input, VectList output){
        if(input.size() > 0){
            output.add(input, 0);
            generalize(input, 0, input.size()-1, output);
        }
    }
    
    //TODO:  We are not checking for constraints here
    public Network generalize(Network input){
        List<VectList> allLines = new ArrayList<>();
        input.extractLines(allLines, true);
        Network output = new Network();
        VectList generalized = new VectList();
        for(VectList lines : allLines){
            generalized.clear();
            generalize(lines, generalized);
            output.addAllLinks(generalized);
        }
        return output;
    }
    
    void generalize(VectList input, int aIndex, int bIndex, VectList output){
        if(aIndex >= bIndex){
            return;
        }
        double ax = input.getX(aIndex);
        double ay = input.getY(aIndex);
        double bx = input.getX(bIndex);
        double by = input.getY(bIndex);
        int maxDistIndex = -1;
        double maxDist = tolerance.toleranceSq;
        for(int i = aIndex+1; i < bIndex; i++){
            double x = input.getX(i);
            double y = input.getY(i);
            double distSq = Line.distSegVectSq(ax, ay, bx, by, x, y);
            if(distSq > maxDist){
                maxDist = distSq;
                maxDistIndex = i;
            }
        }
        if(maxDistIndex >= 0){
            generalize(input, aIndex, maxDistIndex, output);
            generalize(input, maxDistIndex, bIndex, output);
        }else{
            output.add(input, bIndex);
        }
    }
}
