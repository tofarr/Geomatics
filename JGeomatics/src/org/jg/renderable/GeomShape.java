package org.jg.renderable;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.jg.geom.Geom;
import org.jg.geom.Rect;

/**
 *
 * @author tofar_000
 */
public class GeomShape implements Shape {

    private final Geom geom;
    private final Rect bounds;
    private Shape shape;

    public GeomShape(Geom geom) throws NullPointerException {
        if (geom == null) {
            throw new NullPointerException("Geom must not be null");
        }
        this.geom = geom;
        bounds = geom.getBounds();
    }

    @Override
    public Rectangle getBounds() {
        Rectangle ret = new Rectangle();
        ret.add(bounds.minX, bounds.minY);
        ret.add(bounds.maxX, bounds.maxY);
        return ret;
    }

    @Override
    public Rectangle2D getBounds2D() {
        Rectangle2D.Double ret = new Rectangle2D.Double();
        ret.add(bounds.minX, bounds.minY);
        ret.add(bounds.maxX, bounds.maxY);
        return ret;
    }
    
    protected Shape getShape(){
        if(shape == null){
            Path2D.Double path = new Path2D.Double();
            PathIterator iter = geom.pathIterator();
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
        PathIterator ret = geom.pathIterator();
        if((at != null) && (!at.isIdentity())){
            ret = new TransformingPathIterator(ret, at);
        }
        return ret;
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        PathIterator ret = geom.pathIterator();
        if((at != null) && (!at.isIdentity())){
            ret = new TransformingPathIterator(ret, at);
        }
        if(flatness > 0){
            ret = new FlatteningPathIterator(ret, flatness);
        }
        return ret;
    }

}
