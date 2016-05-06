package org.geomatics.gfx.source;

import org.geomatics.gfx.renderable.Renderable;
import org.geomatics.util.View;
import org.geomatics.util.ViewPoint;

/**
 *
 * @author tofarrell
 */
public interface RenderableObjectSource<E> {

    //HOW DO WE RELATE A RENDERABLE OBJECT BACK TO A FEATURE / SET OF FEATURES? WE USE GET ATTRIBUTES OF COURSE!
    
    boolean load(View view, RenderableObjectProcessor processor);
    
    boolean load(ViewPoint viewPoint, RenderableObjectProcessor processor);
    
    E getAttributes(long renderableId);

    interface RenderableObjectProcessor {

        boolean process(Renderable renderable);
    }

}
