package org.jg.gfx.renderable;

import java.awt.Graphics2D;
import org.jg.geom.Rect;
import org.jg.util.Transform;

/**
 * Renderable object
 *
 * @author tofarrell
 */
public interface Renderable {
    
    boolean boundsVariable(); // bounds may be fixed or vary depending on the resolution
        
    Rect toBounds(double resolution);

    void render(Graphics2D g, Transform transform);
}
