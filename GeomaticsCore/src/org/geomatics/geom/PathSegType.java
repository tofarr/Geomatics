package org.geomatics.geom;

/**
 * Path Segment Type. Created to allow easier transpiling to javascript.
 * Contains mappings to corresponding types in java for ease of mapping.
 *
 * @author tofar
 */
public enum PathSegType {
    MOVE(0, 'M'),
    LINE(1, 'L'),
    QUAD(2, 'Q'),
    CUBIC(3, 'C'),
    CLOSE(4, 'Z');

    /**
     * Corresponding type from java path iterator
     */
    public final int javaType;
    
    public final char code;

    private PathSegType(int javaType, char code) {
        this.javaType = javaType;
        this.code = code;
    }
    
    public static PathSegType fromCode(char c){
        switch(c){
            case 'M':
                return MOVE;
            case 'L':
                return LINE;
            case 'Q':
                return QUAD;
            case 'C':
                return CUBIC;
            default: // 'Z'
                return CLOSE;
        }
    }
}
