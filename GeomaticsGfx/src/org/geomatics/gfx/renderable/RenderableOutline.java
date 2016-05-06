package org.geomatics.gfx.renderable;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.beans.ConstructorProperties;
import org.geomatics.geom.Geom;
import org.geomatics.geom.Rect;
import org.geomatics.gfx.util.GeomShape;
import org.geomatics.gfx.util.TransformingShape;
import org.geomatics.gfx.fill.Fill;
import org.geomatics.gfx.outline.Outline;
import org.geomatics.util.Tolerance;
import org.geomatics.util.Transform;

/**
 *
 * @author tofarrell
 */
public class RenderableOutline implements Renderable {

    private final long id;
    private final Geom geom;
    private final Fill fill;
    private final Outline outline;
    private transient Shape shape;

    @ConstructorProperties({"id", "geom", "fill", "outline"})
    public RenderableOutline(long id, Geom geom, Fill fill, Outline outline) {
        this.id = id;
        this.geom = geom;
        this.fill = fill;
        this.outline = outline;
    }

    @Override
    public long getId() {
        return id;
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
        double padding = outline.getPadding() * resolution;
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
        g.draw(_shape);
    }
}
