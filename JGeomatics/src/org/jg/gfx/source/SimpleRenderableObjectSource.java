package org.jg.gfx.source;

import java.beans.ConstructorProperties;
import org.jg.geom.Rect;
import org.jg.geom.Relation;
import org.jg.gfx.renderable.Renderable;
import org.jg.util.Tolerance;
import org.jg.util.View;

/**
 * No indexing - suitable for a few items to be rendered
 * @author tofarrell
 */
public class SimpleRenderableObjectSource implements RenderableObjectSource {

    public static final SimpleRenderableObjectSource EMPTY = new SimpleRenderableObjectSource();
    private final Renderable[] renderables;

    @ConstructorProperties({"renderables"})
    public SimpleRenderableObjectSource(Renderable... renderables) {
        this.renderables = renderables.clone();
    }

    public Renderable[] getRenderables() {
        return renderables.clone();
    }

    @Override
    public boolean load(View view, final RenderableObjectProcessor processor) {
        Rect viewBounds = view.getBounds();
        double resolution = view.getResolution();
        for(Renderable renderable : renderables){
            Rect bounds = renderable.toBounds(resolution);
            if(Relation.isAInsideB(bounds.relate(viewBounds, Tolerance.DEFAULT))){
                if(!processor.process(renderable)){
                    return false;
                }
            }
        }
        return true;
    }

}
