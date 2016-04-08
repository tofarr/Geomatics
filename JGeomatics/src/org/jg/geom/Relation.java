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
    public static final int INSIDE = 1;
    public static final int OUTSIDE = 2;
    public static final int TOUCH = 4;
    
    //Geometry relations
    public static final int INSIDE_OTHER = 8;
    public static final int OUTSIDE_OTHER = 16;
        
    //Some of the combined relations
    public static final int DISJOINT = OUTSIDE | OUTSIDE_OTHER;
    public static final int OVERLAPPING = INSIDE | INSIDE_OTHER;
    public static final int ALL = INSIDE | OUTSIDE | TOUCH | INSIDE_OTHER | OUTSIDE_OTHER;
    
    public static boolean isInside(int relation){
        return (relation & INSIDE) != 0;
    }
    
    public static boolean isOutside(int relation){
        return (relation & OUTSIDE) != 0;
    }
    
    public static boolean isTouch(int relation){
        return (relation & TOUCH) != 0;
    }
    
    public static boolean isInsideOther(int relation){
        return (relation & INSIDE_OTHER) != 0;
    }
    
    public static boolean isOutsideOther(int relation){
        return (relation & OUTSIDE_OTHER) != 0;
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
        if(isInside(ret)){
            ret |= INSIDE_OTHER;
        }
        if(isOutside(ret)){
            ret |= OUTSIDE_OTHER;
        }
        if(isInsideOther(ret)){
            ret |= INSIDE;
        }
        if(isOutsideOther(ret)){
            ret |= OUTSIDE;
        }
        return ret;
    }
}
