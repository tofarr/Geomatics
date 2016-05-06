package org.geomatics.gfx.renderable;

import java.awt.Graphics2D;
import org.geomatics.geom.Rect;
import org.geomatics.util.Transform;

/**
 * Renderable object
 *
 * @author tofarrell
 */
public interface Renderable {
    
    long getId();
    
    boolean boundsVariable(); // bounds may be fixed or vary depending on the resolution
        
    Rect toBounds(double resolution);

    void render(Graphics2D g, Transform transform);
}
