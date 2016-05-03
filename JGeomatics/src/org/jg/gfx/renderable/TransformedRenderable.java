package org.jg.gfx.renderable;

import java.awt.Graphics2D;
import org.jg.geom.Rect;
import org.jg.util.Transform;

/**
 *
 * @author tofarrell
 */
public class TransformedRenderable implements Renderable{

    private final long id;
    private final Renderable renderable;
    private final Transform transform;
    private final Transform inverse;

    public TransformedRenderable(long id, Renderable renderable, Transform transform) {
        this.id = id;
        this.renderable = renderable;
        this.transform = transform;
        this.inverse = transform.getInverse();
    } 
    
    @Override
    public long getId() {
        return id;
    }

    @Override
    public boolean boundsVariable() {
        return renderable.boundsVariable();
    }

    @Override
    public Rect toBounds(double resolution) {
        Rect bounds = renderable.toBounds(resolution);
        Rect ret = bounds.transform(inverse);
        return ret;
    }

    @Override
    public void render(Graphics2D g, Transform transform) {
        Transform toApply = (transform.mode == Transform.NO_OP) ? this.transform : 
                this.transform.toBuilder().add(transform.toBuilder()).build();
        renderable.render(g, toApply);
    }

}
