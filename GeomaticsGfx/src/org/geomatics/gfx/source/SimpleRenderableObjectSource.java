package org.geomatics.gfx.source;

import java.beans.ConstructorProperties;
import org.geomatics.geom.Rect;
import org.geomatics.geom.Relation;
import org.geomatics.geom.Vect;
import org.geomatics.gfx.renderable.Renderable;
import org.geomatics.util.Tolerance;
import org.geomatics.util.View;
import org.geomatics.util.ViewPoint;

/**
 * No indexing - suitable for a few items to be rendered
 * @author tofarrell
 */
public class SimpleRenderableObjectSource implements RenderableObjectSource<Renderable> {

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

    @Override
    public boolean load(ViewPoint viewPoint, RenderableObjectProcessor processor) {
        Vect center = viewPoint.getCenter();
        double resolution = viewPoint.getResolution();
        for(Renderable renderable : renderables){
            Rect bounds = renderable.toBounds(resolution);
            if(Relation.isAInsideB(bounds.relate(center, Tolerance.DEFAULT))){
                if(!processor.process(renderable)){
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Renderable getAttributes(long renderableId) {
        for(Renderable renderable : renderables){
            if(renderable.getId() == renderableId){
                return renderable;
            }
        }
        return null;
    }
    

}
