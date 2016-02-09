package org.jg;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

/**
 * For rendering, most of the methods in java.awt.Shape are not called. This class
 * allows other objects to implement a less complex interface and in most cases
 * avoid a conversion.
 * @author tofar_000
 */
public interface PathIterable {
    
    Rect getBounds(Rect target);
    
    PathIterator getPathIterator();
    
    PathIterator getPathIterator(AffineTransform transform);
    
    PathIterator getPathIterator(AffineTransform transform, double flatness);
}
