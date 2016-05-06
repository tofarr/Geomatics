package org.geomatics.gfx.outline;

import java.awt.Stroke;

/**
 *
 * @author tofarrell
 */
public interface Outline {

    double getPadding();
    
    Stroke toStroke();
}
