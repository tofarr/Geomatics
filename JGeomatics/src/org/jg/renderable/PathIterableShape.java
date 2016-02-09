package org.jg.renderable;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.jg.PathIterable;
import org.jg.Rect;

/**
 *
 * @author tofar_000
 */
public class PathIterableShape implements Shape {

    private final PathIterable iterable;
    private final Rect bounds;
    private Shape shape;

    public PathIterableShape(PathIterable iterable) throws NullPointerException {
        if (iterable == null) {
            throw new NullPointerException("Iterable must not be null");
        }
        this.iterable = iterable;
        bounds = iterable.getBounds(new Rect());
    }

    @Override
    public Rectangle getBounds() {
        Rectangle ret = new Rectangle();
        ret.add(bounds.getMinX(), bounds.getMinY());
        ret.add(bounds.getMaxX(), bounds.getMaxY());
        return ret;
    }

    @Override
    public Rectangle2D getBounds2D() {
        Rectangle2D.Double ret = new Rectangle2D.Double();
        ret.add(bounds.getMinX(), bounds.getMinY());
        ret.add(bounds.getMaxX(), bounds.getMaxY());
        return ret;
    }
    
    protected Shape getShape(){
        if(shape == null){
            Path2D.Double path = new Path2D.Double();
            PathIterator iter = iterable.getPathIterator();
            double[] coords = new double[6];
            while(!iter.isDone()){
                switch(iter.currentSegment(coords)){
                    case PathIterator.SEG_CLOSE:
                        path.closePath();
                        break;
                    case PathIterator.SEG_CUBICTO:
                        path.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                        break;
                    case PathIterator.SEG_LINETO:
                        path.lineTo(coords[0], coords[1]);
                        break;
                    case PathIterator.SEG_MOVETO:
                        path.moveTo(coords[0], coords[1]);
                        break;
                    case PathIterator.SEG_QUADTO:
                        path.quadTo(coords[0], coords[1], coords[2], coords[3]);
                        break;
                }
                iter.next();
            }
            shape = path;
        }
        return shape;
    }

    @Override
    public boolean contains(double x, double y) {
        return getShape().contains(x, y);
    }

    @Override
    public boolean contains(Point2D p) {
        return getShape().contains(p);
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        return getShape().intersects(x, y, w, h);
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        return getShape().intersects(r);
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        return getShape().contains(x, y, w, h);
    }

    @Override
    public boolean contains(Rectangle2D r) {
        return getShape().contains(r);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        return iterable.getPathIterator(at);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return iterable.getPathIterator(at, flatness);
    }

}
