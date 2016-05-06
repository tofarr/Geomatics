package org.geomatics.gfx.renderable;

import java.awt.Graphics2D;
import java.beans.ConstructorProperties;
import org.geomatics.geom.Rect;
import org.geomatics.geom.Vect;
import org.geomatics.util.Transform;
import org.geomatics.util.TransformBuilder;

/**
 *
 * @author tofarrell
 */
public class FloatingRenderable implements Renderable {

    private final long id;
    private final Renderable renderable;
    private final Vect location;

    @ConstructorProperties({"id", "renderable", "location"})
    public FloatingRenderable(long id, Renderable renderable, Vect location) {
        this.id = id;
        this.renderable = renderable;
        this.location = location;
    }

    @Override
    public long getId() {
        return id;
    }

    public Renderable getRenderable() {
        return renderable;
    }

    public Vect getLocation() {
        return location;
    }

    @Override
    public boolean boundsVariable() {
        return true;
    }

    @Override
    public Rect toBounds(double resolution) {
        Rect bounds = renderable.toBounds(1);
        return Rect.valueOf(bounds.minX + location.x,
                bounds.minY + location.y,
                bounds.maxX + location.x,
                bounds.maxY + location.y);
    }

    @Override
    public void render(Graphics2D g, Transform transform) {
        Vect transformed = transform.transform(location);
        transform = new TransformBuilder().translate(transformed.x, transformed.y).build();
        renderable.render(g, transform);
    }
}
