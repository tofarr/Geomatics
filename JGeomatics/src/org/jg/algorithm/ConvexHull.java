package org.jg.algorithm;

import org.jg.geom.Line;
import org.jg.util.VectList;

/**
 * Implementation of the quickhull algorithm
 * @author tofarrell
 */
public class ConvexHull {
    
    private static ConvexHull INSTANCE = new ConvexHull(); // for code coverage
    
    private ConvexHull(){
    }
    
    /**
     * Get the convex hull of the vects given
     * @param vects
     * @throws NullPointerException if vects was null
     * @return convex hull
     */
    public static VectList getConvexHull(VectList vects) throws NullPointerException{
        if(vects.size() < 2){
            return vects.clone();
        }
        
        //Get points with min and max X value - these must be part of the convex hull
        int minIndex = 0;
        int maxIndex = 0;
        double minX = vects.getX(0);
        double maxX = minX;
        for(int i = vects.size(); i-- > 1;){
            double x = vects.getX(i);
            if((x < minX) || ((x == minX) && (vects.getY(i) < vects.getY(minIndex)))){
                minX = x;
                minIndex = i;
            }
            if(x > maxX){
                maxX = x;
                maxIndex = i;
            }
        }
        
        double minY = vects.getY(minIndex);
        double maxY = vects.getY(maxIndex);
        
        VectList results = new VectList();
        
        if(minX == maxX){ // we have a point or a vertical line
            for(int i = vects.size(); i-- > 0;){
                double y = vects.getY(i);
                if(y > maxY){
                    maxY = y;
                }
            }
            results.add(minX, minY);
            results.add(maxX, maxY);
            results.add(minX, minY);
            return results;
        }else{
            results.add(minX, minY);
            divide(minX, minY, maxX, maxY, vects, results);
            divide(maxX, maxY, minX, minY, vects, results);
            return results;
        }
    }
    
    
    static void divide(double ax, double ay, double bx, double by, VectList input, VectList output){
        
        //Get only points to the right of the line...
        VectList right = new VectList();
        filterRight(ax, ay, bx, by, input, right);
        
        int index = indexOfFurthestPointFromLine(ax, ay, bx, by, right);
        if(index >= 0){
            double x = right.getX(index);
            double y = right.getY(index);
            divide(ax, ay, x, y, right, output);
            divide(x, y, bx, by, right, output);
        }else{
            output.add(bx, by);
        }
    }
    
    /** Get the point from the list given furthest from the line given.*/
    static int indexOfFurthestPointFromLine(double ax, double ay, double bx, double by, VectList input){
        double maxValue = 0;
        int maxIndex = -1;
        for(int i = 0; i < input.size(); i++){
            double x = input.getX(i);
            double y = input.getY(i);
            double d = Line.distLineVectSq(ax, ay, bx, by, x, y);
            if(d > maxValue){
                maxValue = d;
                maxIndex = i;
            }
        }
        return maxIndex;
    }
    
    /**
     * Identify points from input which are to the left of the line given, and place them in output
     * given
     */
    static void filterRight(double ax, double ay, double bx, double by, VectList input, VectList output){
        for(int i = 0; i < input.size(); i++){
            double x = input.getX(i);
            double y = input.getY(i);
            if(Line.counterClockwise(ax, ay, bx, by, x, y) > 0){
                output.add(x, y);
            }
        }
    }
    
}
