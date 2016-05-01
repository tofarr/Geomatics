package org.jg.geom;

/**
 * PathIterator - created to allow easier transpiling (And reducing the number
 * of methods each implementation must implement
 *
 * @author tofar
 */
public interface PathIter {

    boolean isDone();

    void next();

    PathSegType currentSegment(double[] coords) throws IllegalStateException;
}
