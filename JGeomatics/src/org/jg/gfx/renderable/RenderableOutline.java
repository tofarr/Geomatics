package org.jg.gfx.renderable;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.beans.ConstructorProperties;
import org.jg.geom.Geom;
import org.jg.geom.Rect;
import org.jg.gfx.util.GeomShape;
import org.jg.gfx.util.TransformingShape;
import org.jg.gfx.fill.Fill;
import org.jg.gfx.outline.Outline;
import org.jg.util.Tolerance;
import org.jg.util.Transform;

/**
 *
 * @author tofarrell
 */
public class RenderableOutline implements Renderable {

    private final Geom geom;
    private final Fill fill;
    private final Outline outline;
    private transient Shape shape;

    @ConstructorProperties({"geom", "fill", "outline"})
    public RenderableOutline(Geom geom, Fill fill, Outline outline) {
        this.geom = geom;
        this.fill = fill;
        this.outline = outline;
    }

    public Geom getGeom() {
        return geom;
    }

    public Fill getFill() {
        return fill;
    }

    public Outline getOutline() {
        return outline;
    }

    @Override
    public boolean boundsVariable() {
        return true;
    }

    @Override
    public Rect toBounds(double resolution) {
        double padding = outline.getPadding();
        return geom.getBounds().buffer(padding);
    }

    public Shape toShape() {
        Shape ret = shape;
        if (ret == null) {
            ret = new GeomShape(geom, Tolerance.DEFAULT);
            shape = ret;
        }
        return ret;
    }

    @Override
    public void render(Graphics2D g, Transform transform) {
        g.setPaint(fill.toPaint());
        g.setStroke(outline.toStroke());
        Shape _shape = toShape();
        _shape = TransformingShape.valueOf(shape, transform);
        g.fill(_shape);
    }
}
