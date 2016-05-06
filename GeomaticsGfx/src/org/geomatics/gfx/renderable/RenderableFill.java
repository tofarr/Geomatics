package org.geomatics.gfx.renderable;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.beans.ConstructorProperties;
import org.geomatics.geom.Geom;
import org.geomatics.geom.Rect;
import org.geomatics.gfx.util.GeomShape;
import org.geomatics.gfx.util.TransformingShape;
import org.geomatics.gfx.fill.Fill;
import org.geomatics.util.Tolerance;
import org.geomatics.util.Transform;

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
