package org.jg.gfx.source;

import java.beans.ConstructorProperties;
import java.util.Map;
import org.jg.geom.Rect;
import org.jg.gfx.renderable.Renderable;
import org.jg.util.RTree;
import org.jg.util.SpatialNode;
import org.jg.util.SpatialNode.NodeProcessor;
import org.jg.util.View;

/**
 *
 * @author tofarrell
 */
public class SimpleRenderableObjectSource implements RenderableObjectSource {

    private final Renderable[] renderables;
    private final Map<Double,SpatialNode<Renderable>> indices;

    @ConstructorProperties({"renderables"})
    public SimpleRenderableObjectSource(Renderable... renderables) {
        this.renderables = renderables.clone();
        indices = new HashMap<>();
    }

    public Renderable[] getRenderables() {
        return renderables.clone();
    }

    @Override
    public boolean load(View view, final RenderableObjectProcessor processor) {
        double resolution = Math.max(view.getResolutionX(), view.getResolutionY());
        SpatialNode<Renderable> index = indices.get(resolution);
        if(index == null){
            Rect[] bounds = new Rect[renderables.length];
            for(int r = 0; r < renderables.length; r++){
                bounds[r] = renderables[r].toBounds(resolution);
            }
            RTree<Renderable> tree = new RTree<>(bounds, renderables);
            index = tree.getRoot();
            indices.put(resolution, index);
        }
        return index.forOverlapping(view.getBounds(), new NodeProcessor<Renderable>() {
            @Override
            public boolean process(Rect bounds, Renderable renderable) {
                processor.process(renderable);
                return true;
            }
        });
    }

}
