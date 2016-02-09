package org.jg.renderable;

import java.awt.Graphics2D;
import org.jg.Transform;

/**
 *
 * @author tofar_000
 */
public interface Renderable {

    void render(Graphics2D g, Transform transform);
}
