package org.jg.geom;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.beans.Transient;
import java.io.IOException;
import org.jg.Network;
import org.jg.Relate;
import org.jg.util.Transform;

/**
 *
 * @author tofar_000
 */
public class Rect implements Geom {

    public final double minX;
    public final double minY;
    public final double maxX;
    public final double maxY;

    Rect(double minX, double minY, double maxX, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public static Rect valueOf(double minX, double minY, double maxX, double maxY) throws IllegalArgumentException {
        check(minX, minY, maxX, maxY);
        if (minX > maxX) {
            double tmp = minX;
            minX = maxX;
            maxX = tmp;
        }
        if (minY > maxY) {
            double tmp = minY;
            minY = maxY;
            maxY = tmp;
        }
        return new Rect(minX, minY, maxX, maxY);
    }

    /**
     * Get width. Note: May be invalid if rect is invalid
     *
     * @return
     */
    @Transient
    public double getWidth() {
        return maxX - minX;
    }

    /**
     * Get height Note: May be invalid if rect is invalid
     *
     * @return
     */
    @Transient
    public double getHeight() {
        return maxY - minY;
    }

    /**
     * Get area Note: May be invalid if rect is invalid
     *
     * @return
     */
    @Transient
    public double getArea() {
        return getWidth() * getHeight();
    }

    /**
     * Get centroid x
     *
     * @return
     */
    @Transient
    public double getCx() {
        return (minX + maxX) / 2;
    }

    /**
     * Get centroid y
     *
     * @return
     */
    @Transient
    public double getCy() {
        return (minY + maxY) / 2;
    }

    @Transient
    public Vect getCentroid() {
        return new Vect(getCx(), getCy());
    }

    /**
     * Determine if this rect is disjoint from that given (does not touch or
     * share any internal area). Invalid rects are considered disjoint.
     *
     * @param rect
     * @return true if rects are disjoint or rect was null, false otherwise
     * @throws NullPointerException if rect was null
     */
    public boolean isDisjoint(Rect rect) throws NullPointerException {
        return disjoint(minX, minY, maxX, maxY, rect.minX, rect.minY, rect.maxX, rect.maxY);
    }

    /**
     * Determine if this rect overlaps (Shares some internal area with) that
     * given. Invalid rects are considered disjoint, and never overlap
     *
     * @param rect
     * @return true if overlapping, false otherwise
     */
    public boolean isOverlapping(Rect rect) {
        return overlaps(minX, minY, maxX, maxY, rect.minX, rect.minY, rect.maxX, rect.maxY);
    }

    /**
     * Determine if this rect covers the rect given. (ie: No part of rect is
     * outside this) Invalid rects cannot overlap, and so cannot contain other
     * rects or be contained within other rects
     *
     * @param rect
     * @return true if contains rect, false otherwise or if rect was invalid
     * @throws NullPointerException if rect was null
     */
    public boolean contains(Rect rect) throws NullPointerException {
        return contains(minX, minY, maxX, maxY, rect.minX, rect.minY, rect.maxX, rect.maxY);
    }

    static boolean contains(double aMinX, double aMinY, double aMaxX, double aMaxY,
            double bMinX, double bMinY, double bMaxX, double bMaxY) {
        return (aMinX <= bMinX) && (aMinY <= bMinY) && (aMaxX >= bMaxX) && (aMaxY >= bMaxY);
    }

    /**
     * Determine if this rect covers the vect given. (inside or touching)
     *
     * @param vect
     * @return true if contains vect, false otherwise
     * @throws NullPointerException if vect was null
     */
    public boolean contains(Vect vect) throws NullPointerException {
        return contains(minX, minY, maxX, maxY, vect.getX(), vect.getY());
    }

    /**
     * Determine if the rect given contains the point given. (Is inside or
     * touching)
     *
     * @param aMinX
     * @param aMinY
     * @param aMaxX
     * @param aMaxY
     * @param x
     * @param y
     * @return
     */
    public static boolean contains(double aMinX, double aMinY, double aMaxX, double aMaxY,
            double x, double y) {
        return (aMinX <= x) && (aMinY <= y) && (aMaxX >= x) && (aMaxY >= y);
    }

    /**
     * Get the relation between this rect and the vect given
     *
     * @param vect
     * @return relation
     * @throws NullPointerException if vect was null
     */
    public Relate relate(Vect vect) throws NullPointerException {
        return relate(minX, minY, maxX, maxY, vect.getX(), vect.getY());
    }

    static Relate relate(double minX, double minY, double maxX, double maxY,
            double x, double y) {
        if ((x < minX) || (y < minY) || (x > maxX) || (y > maxY)) {
            return Relate.OUTSIDE;
        } else if ((x == minX) || (y == minY) || (x == maxX) || (y == maxY)) {
            return Relate.TOUCH;
        } else {
            return Relate.INSIDE;
        }
    }

    /**
     * Get the intersection between this and the rect given
     *
     * @param rect
     * @return result
     * @throws NullPointerException if rect was null
     */
    public Rect intersection(Rect rect) throws NullPointerException {
        if (isDisjoint(rect)) {
            return null;
        } else {
            return valueOf(Math.max(minX, rect.minX),
                    Math.max(minY, rect.minY),
                    Math.min(maxX, rect.maxX),
                    Math.min(maxY, rect.maxY));
        }
    }

    /**
     * Get the intersection between this and the rect given
     *
     * @param rect
     * @return result
     * @throws NullPointerException if rect was null
     */
    public Rect union(Rect rect) throws NullPointerException {
        return new Rect(Math.min(minX, rect.minX),
                Math.min(minY, rect.minY),
                Math.max(maxX, rect.maxX),
                Math.max(maxY, rect.maxY));
    }
    
    
    /**
     * Get a Rect based on this buffered by the amount given
     *
     * @param amt
     * @return buffered Rect
     * @throws IllegalArgumentException if amt was infinite or NaN
     */
    public Rect buffer(double amt) throws IllegalArgumentException {
        Vect.check(amt, "Invalid buffer amount {0}");
        if (amt == 0) {
            return this;
        }
        if (amt < 0) {
            if (((minX - amt) > (maxX + amt)) || ((minY - amt) > (maxY + amt))) {
                return null;
            }
        }
        return new Rect(minX - amt,
                minY - amt,
                maxX + amt,
                maxY + amt);
    }


    @Override
    @Transient
    public Rect getBounds() {
        return this;
    }

    @Override
    public Rect transform(Transform transform) throws NullPointerException {
        RectBuilder rect = new RectBuilder();
        VectBuilder vect = new VectBuilder();
        vect.set(minX, minY);
        transform.transform(vect, vect);
        rect.add(vect);
        vect.set(minX, maxY);
        transform.transform(vect, vect);
        rect.add(vect);
        vect.set(maxX, minY);
        transform.transform(vect, vect);
        rect.add(vect);
        vect.set(maxX, maxY);
        transform.transform(vect, vect);
        rect.add(vect);
        return rect.build();
    }

    @Override
    public PathIterator pathIterator() {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(minX, minY);
        path.lineTo(minX, maxY);
        path.lineTo(maxX, maxY);
        path.lineTo(maxX, minY);
        path.closePath();
        return path.getPathIterator(null);
    }

    @Override
    public void addTo(Network network, double flatness) throws NullPointerException, IllegalArgumentException {
        network.addLink(minX, minY, minX, maxY);
        network.addLink(minX, minY, maxX, minY);
        network.addLink(minX, maxY, maxX, maxY);
        network.addLink(maxX, minY, maxX, maxY);
    }

    
    @Override
    public void addBoundsTo(RectBuilder target) throws NullPointerException {
        target.addInternal(minX, minY);
        target.addInternal(maxX, maxY);
    }

    @Override
    public Rect clone() {
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Vect.hash(minX);
        hash = 43 * hash + Vect.hash(minY);
        hash = 43 * hash + Vect.hash(maxX);
        hash = 43 * hash + Vect.hash(maxY);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Rect) {
            Rect rect = (Rect) obj;
            return (minX == rect.minX)
                    && (minY == rect.minY)
                    && (maxX == rect.maxX)
                    && (maxY == rect.maxY);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return toString(minX, minY, maxX, maxY);
    }

    @Override
    public void toString(Appendable appendable) throws NullPointerException, GeomException {
        toString(minX, minY, maxX, maxY, appendable);
    }

    static String toString(double minX, double minY, double maxX, double maxY) {
        StringBuilder str = new StringBuilder();
        toString(minX, minY, maxX, maxY, str);
        return str.toString();
    }

    static void toString(double minX, double minY, double maxX, double maxY, Appendable appendable) {
        try {
            appendable.append('[')
                    .append(Vect.ordToStr(minX)).append(',')
                    .append(Vect.ordToStr(minY)).append(',')
                    .append(Vect.ordToStr(maxX)).append(',')
                    .append(Vect.ordToStr(maxY)).append(',')
                    .append(']');
        } catch (IOException ex) {
            throw new GeomException("Error writing", ex);
        }
    }

    static void check(double minX, double minY, double maxX, double maxY) {
        Vect.check(minX, "Invalid minX : {0}");
        Vect.check(minY, "Invalid minY : {0}");
        Vect.check(maxX, "Invalid maxX : {0}");
        Vect.check(maxY, "Invalid maxY : {0}");
    }

    static boolean disjoint(double aMinX, double aMinY, double aMaxX, double aMaxY,
            double bMinX, double bMinY, double bMaxX, double bMaxY) {
        return (aMinX > bMaxX) || (aMinY > bMaxY) || (aMaxX < bMinX) || (aMaxY < bMinY);
    }

    static boolean overlaps(double aMinX, double aMinY, double aMaxX, double aMaxY,
            double bMinX, double bMinY, double bMaxX, double bMaxY) {
        return (aMinX < bMaxX) && (aMinY < bMaxY) && (aMaxX > bMinX) && (aMaxY > bMinY);
    }

}
