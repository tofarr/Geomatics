package org.geomatics.gfx.util;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.geomatics.geom.VectBuilder;
import org.geomatics.util.Transform;
import org.geomatics.util.TransformBuilder;

/**
 *
 * @author tofarrell
 */
public class TransformingShape implements Shape {

    private final Shape shape;
    private final Transform transform;
    private final Transform inverse;

    private TransformingShape(Shape shape, Transform transform, Transform inverse) {
        this.shape = shape;
        this.transform = transform;
        this.inverse = inverse;
    }
    
    public static Shape valueOf(Shape shape, Transform transform){
        if(transform.mode == Transform.NO_OP){
            return shape;
        }
        return new TransformingShape(shape, transform, transform.getInverse());
    }

    @Override
    public Rectangle getBounds() {
        Rectangle bounds = shape.getBounds();
        VectBuilder pnt = new VectBuilder();
        Rectangle rect = new Rectangle(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        add(pnt, bounds.getMinX(), bounds.getMinY(), rect);
        add(pnt, bounds.getMinX(), bounds.getMaxY(), rect);
        add(pnt, bounds.getMaxX(), bounds.getMinY(), rect);
        add(pnt, bounds.getMaxX(), bounds.getMaxY(), rect);
        return rect;
    }
    
    private void add(VectBuilder pnt, double x, double y, Rectangle rect){
        pnt.set(x, y);
        transform.transform(pnt, pnt);
        rect.add((int)Math.round(pnt.getX()), (int)Math.round(pnt.getY()));
    }

    @Override
    public Rectangle2D getBounds2D() {
        Rectangle2D bounds = shape.getBounds2D();
        VectBuilder pnt = new VectBuilder();
        Rectangle2D rect = new Rectangle2D.Double(Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
        add(pnt, bounds.getMinX(), bounds.getMinY(), rect);
        add(pnt, bounds.getMinX(), bounds.getMaxY(), rect);
        add(pnt, bounds.getMaxX(), bounds.getMinY(), rect);
        add(pnt, bounds.getMaxX(), bounds.getMaxY(), rect);
        return rect;
    }

    private void add(VectBuilder pnt, double x, double y, Rectangle2D rect){
        pnt.set(x, y);
        transform.transform(pnt, pnt);
        rect.add((int)Math.round(pnt.getX()), (int)Math.round(pnt.getY()));
    }
    
    @Override
    public boolean contains(double x, double y) {
        VectBuilder vect = new VectBuilder(x, y);
        inverse.transform(vect, vect);
        return shape.contains(vect.getX(), vect.getY());
    }

    @Override
    public boolean contains(Point2D p) {
        return contains(p.getX(), p.getY());
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        return getBounds2D().intersects(x, y, w, h);
    }
    
    @Override
    public boolean intersects(Rectangle2D r) {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        return false;
    }

    @Override
    public boolean contains(Rectangle2D r) {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        Transform _transform = transform;
        if((at != null) && (!at.isIdentity())){
            _transform = transform.toBuilder().add(new TransformBuilder(at)).build();
        }
        return shape.getPathIterator(_transform.toAffineTransform());
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        Transform _transform = transform;
        if((at != null) && (!at.isIdentity())){
            _transform = transform.toBuilder().add(new TransformBuilder(at)).build();
        }
        return shape.getPathIterator(_transform.toAffineTransform(), flatness);
    }
    
}
