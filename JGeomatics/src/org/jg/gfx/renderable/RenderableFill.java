package org.jg.gfx.renderable;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.beans.ConstructorProperties;
import org.jg.geom.Geom;
import org.jg.geom.Rect;
import org.jg.gfx.util.GeomShape;
import org.jg.gfx.util.TransformingShape;
import org.jg.gfx.fill.Fill;
import org.jg.util.Tolerance;
import org.jg.util.Transform;

/**
 *
 * @author tofarrell
 */
public class RenderableFill implements Renderable {

    private final long id;
    private final Geom geom;
    private final Fill fill;
    private transient Shape shape;

    @ConstructorProperties({"id", "geom", "fill"})
    public RenderableFill(long id, Geom geom, Fill fill) {
        this.id = id;
        this.geom = geom;
        this.fill = fill;
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

    @Override
    public boolean boundsVariable() {
        return false;
    }

    @Override
    public Rect toBounds(double resolution) {
        return geom.getBounds();
    }
    
    public Shape toShape(){
        Shape ret = shape;
        if(ret == null){
            ret = new GeomShape(geom, Tolerance.DEFAULT);
            shape = ret;
        }
        return ret;
    }

    @Override
    public void render(Graphics2D g, Transform transform) {
        g.setPaint(fill.toPaint());
        Shape _shape = toShape();
        _shape = TransformingShape.valueOf(shape, transform);
        g.fill(_shape);
    }
}
