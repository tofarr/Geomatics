package org.jg.gfx.source;

import org.jg.gfx.renderable.Renderable;
import org.geomatics.util.View;

/**
 *
 * @author tofarrell
 */
public interface RenderableObjectSource {

    boolean load(View view, RenderableObjectProcessor processor);

    interface RenderableObjectProcessor {

        boolean process(Renderable renderable);
    }

}
