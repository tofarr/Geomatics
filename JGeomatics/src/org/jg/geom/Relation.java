package org.jg.geom;

/**
 * Helper class for geometry relation bitmasks.
 * @author tofarrell
 */
public class Relation {
    
    
    //Point relations
    /**
     * No relation has yet been calculated
     */
    public static final int NULL = 0;
    public static final int B_INSIDE_A = 1;
    public static final int B_OUTSIDE_A = 2;
    public static final int TOUCH = 4;
    
    //Geometry relations
    public static final int A_INSIDE_B = 8;
    public static final int A_OUTSIDE_B = 16;
        
    //Some of the combined relations
    public static final int DISJOINT = B_OUTSIDE_A | A_OUTSIDE_B;
    public static final int OVERLAPPING = B_INSIDE_A | A_INSIDE_B;
    public static final int ALL = B_INSIDE_A | B_OUTSIDE_A | TOUCH | A_INSIDE_B | A_OUTSIDE_B;
    
    public static boolean isInside(int relation){
        return (relation & B_INSIDE_A) != 0;
    }
    
    public static boolean isOutside(int relation){
        return (relation & B_OUTSIDE_A) != 0;
    }
    
    public static boolean isTouch(int relation){
        return (relation & TOUCH) != 0;
    }
    
    public static boolean isInsideOther(int relation){
        return (relation & A_INSIDE_B) != 0;
    }
    
    public static boolean isOutsideOther(int relation){
        return (relation & A_OUTSIDE_B) != 0;
    }
    
    public static boolean isOverlapping(int relation){
        return (relation & OVERLAPPING) == OVERLAPPING;
    }
    
    public static boolean isDisjoint(int relation){
        return relation == DISJOINT;
    }
    
    //swap this and other
    public static int swap(int relation){
        int ret = relation & TOUCH;
        if(isInside(relation)){
            ret |= A_INSIDE_B;
        }
        if(isOutside(relation)){
            ret |= A_OUTSIDE_B;
        }
        if(isInsideOther(relation)){
            ret |= B_INSIDE_A;
        }
        if(isOutsideOther(relation)){
            ret |= B_OUTSIDE_A;
        }
        return ret;
    }
}
