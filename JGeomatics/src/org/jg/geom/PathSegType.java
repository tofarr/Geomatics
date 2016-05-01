package org.jg.geom;

/**
 * Path Segment Type. Created to allow easier transpiling to javascript.
 * Contains mappings to corresponding types in java for ease of mapping.
 *
 * @author tofar
 */
public enum PathSegType {
    MOVE(0),
    LINE(1),
    QUAD(2),
    CUBIC(3),
    CLOSE(4);

    /**
     * Corresponding type from java path iterator
     */
    public final int javaType;

    private PathSegType(int javaType) {
        this.javaType = javaType;
    }
}
