package org.jg.gfx.util;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.geomatics.geom.Geom;
import org.geomatics.geom.Rect;
import org.geomatics.geom.Relation;
import org.geomatics.geom.Vect;
import org.geomatics.util.Tolerance;

/**
 *
 * @author tofar_000
 */
public class GeomShape implements Shape {

    private final Geom geom;
    private final Tolerance accuracy;

    public GeomShape(Geom geom, Tolerance accuracy) throws NullPointerException {
        if (geom == null) {
            throw new NullPointerException("geom must not be null");
        }
        if (accuracy == null) {
            throw new NullPointerException("accuracy must not be null");
        }
        this.geom = geom;
        this.accuracy = accuracy;
    }

    @Override
    public Rectangle getBounds() {
        Rectangle ret = new Rectangle();
        Rect bounds = geom.getBounds();
        ret.add(bounds.minX, bounds.minY);
        ret.add(bounds.maxX, bounds.maxY);
        return ret;
    }

    @Override
    public Rectangle2D getBounds2D() {
        Rectangle2D.Double ret = new Rectangle2D.Double();
        Rect bounds = geom.getBounds();
        ret.add(bounds.minX, bounds.minY);
        ret.add(bounds.maxX, bounds.maxY);
        return ret;
    }
    
    @Override
    public boolean contains(double x, double y) {
        return Relation.isBInsideA(geom.relate(Vect.valueOf(x, y), accuracy));
    }

    @Override
    public boolean contains(Point2D p) {
        return contains(p.getX(), p.getY());
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        Rect rect = Rect.valueOf(x, y, x+w, y+h);
        return Relation.isAInsideB(rect.relate(geom.getBounds(), accuracy));
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        return intersects(r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight());
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        return false;
    }

    @Override
    public boolean contains(Rectangle2D r) {
        return contains(r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight());
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        PathIterator ret = new IterPathIterator(geom.iterator());
        ret = TransformingPathIterator.valueOf(ret, at);
        return ret;
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        PathIterator ret = new IterPathIterator(geom.iterator());
        ret = TransformingPathIterator.valueOf(ret, at);
        if(flatness > 0){
            ret = new FlatteningPathIterator(ret, flatness);
        }
        return ret;
    }

}
