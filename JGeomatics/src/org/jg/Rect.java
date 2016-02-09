package org.jg;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.beans.ConstructorProperties;
import java.beans.Transient;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Rectangle where each ordinate may not be infinite or NaN. If min > max, rect
 * is considered invalid. An invalid rect may not return valid values for width,
 * height, or area.
 *
 * @author tim.ofarrell
 */
public final class Rect implements PathIterable, Cloneable, Externalizable {

    double minX;
    double minY;
    double maxX;
    double maxY;

    /**
     * Crreate a new invalid Rect
     */
    public Rect() {
        reset();
    }

    /**
     * Reset this rect to empty
     * [Double.MAX_VALUE,Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE]
     *
     * @return this
     */
    public Rect reset() {
        minX = minY = Double.MAX_VALUE;
        maxX = maxY = -Double.MAX_VALUE;
        return this;
    }

    /**
     * Create a new rect based on that given
     *
     * @param rect
     * @throws NullPointerException if rect was null
     */
    public Rect(Rect rect) throws NullPointerException {
        set(rect);
    }

    /**
     * Set the ordinates of this rect to match those given
     *
     * @param rect
     * @return this
     * @throws NullPointerException if rect was null
     */
    public Rect set(Rect rect) throws NullPointerException {
        this.minX = rect.minX;
        this.minY = rect.minY;
        this.maxX = rect.maxX;
        this.maxY = rect.maxY;
        return this;
    }

    /**
     * Create a new instance of rect. Does not perform validation checks
     *
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     * @throws IllegalArgumentException if an ordinate was infinite or NaN
     */
    @ConstructorProperties({"minX", "minY", "maxX", "maxY"})
    public Rect(double minX, double minY, double maxX, double maxY) throws IllegalArgumentException {
        set(minX, minY, maxX, maxY);
    }

    /**
     * Set this rect to the values given
     *
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     * @return this
     */
    public Rect set(double minX, double minY, double maxX, double maxY) throws IllegalArgumentException {
        Util.check(minX, "Invalid minX : {0}");
        Util.check(minY, "Invalid minY : {0}");
        Util.check(maxX, "Invalid maxX : {0}");
        Util.check(maxY, "Invalid maxY : {0}");
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        return this;
    }

    /**
     * Normalize this rectangle. If min values are greater than max values, swap
     * them
     *
     * @return this;
     */
    public Rect normalize() {
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
        return this;
    }

    /**
     * Get minX
     *
     * @return
     */
    public double getMinX() {
        return minX;
    }

    /**
     * Get minY
     *
     * @return
     */
    public double getMinY() {
        return minY;
    }

    /**
     * Get maxX
     *
     * @return
     */
    public double getMaxX() {
        return maxX;
    }

    /**
     * Get maxY
     *
     * @return
     */
    public double getMaxY() {
        return maxY;
    }

    /**
     * Determine if this Rect is valid (minX <= maxX) && (minY <= maxY)
     *
     * @return
     */
    @Transient
    public boolean isValid() {
        return (minX <= maxX) && (minY <= maxY);
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

    /**
     * Get centroid Note: May be invalid if rect is invalid
     *
     * @param target
     * @return
     */
    public Vect getCentroid(Vect target) {
        target.set(getCx(), getCy());
        return target;
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
        return (rect.isValid() && isValid()) ? disjoint(minX, minY, maxX, maxY, rect.minX, rect.minY, rect.maxX, rect.maxY) : true;
    }

    static boolean disjoint(double aMinX, double aMinY, double aMaxX, double aMaxY,
            double bMinX, double bMinY, double bMaxX, double bMaxY) {
        return (aMinX > bMaxX) || (aMinY > bMaxY) || (aMaxX < bMinX) || (aMaxY < bMinY);
    }

    /**
     * Determine if this rect overlaps (Shares some internal area with) that
     * given. Invalid rects are considered disjoint, and never overlap
     *
     * @param rect
     * @return true if overlapping, false otherwise
     */
    public boolean isOverlapping(Rect rect) {
        return (rect.isValid() && isValid()) ? overlaps(minX, minY, maxX, maxY, rect.minX, rect.minY, rect.maxX, rect.maxY) : false;
    }

    static boolean overlaps(double aMinX, double aMinY, double aMaxX, double aMaxY,
            double bMinX, double bMinY, double bMaxX, double bMaxY) {
        return (aMinX < bMaxX) && (aMinY < bMaxY) && (aMaxX > bMinX) && (aMaxY > bMinY);
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
        return (rect.isValid() && isValid()) ? contains(minX, minY, maxX, maxY, rect.minX, rect.minY, rect.maxX, rect.maxY) : false;
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
     * Determine if the rect given contains the point given. (Is inside or touching)
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
     * Get the intersection between this and the rect given, and place it in the
     * rect given
     *
     * @param rect
     * @param result
     * @return result
     * @throws NullPointerException if rect or result was null
     */
    public Rect intersection(Rect rect, Rect result) throws NullPointerException {
        if (isDisjoint(rect)) {
            result.reset();
        } else {
            result.minX = Math.max(minX, rect.minX);
            result.minY = Math.max(minY, rect.minY);
            result.maxX = Math.min(maxX, rect.maxX);
            result.maxY = Math.min(maxY, rect.maxY);
        }
        return result;
    }

    /**
     * Get the union between this and the rect given.
     *
     * @param rect
     * @param result
     * @return result
     */
    public Rect union(Rect rect, Rect result) throws NullPointerException {
        if (!rect.isValid()) {
            result.set(this);
        } else if (!isValid()) {
            result.set(rect);
        } else {
            result.minX = Math.min(minX, rect.minX);
            result.minY = Math.min(minY, rect.minY);
            result.maxX = Math.max(maxX, rect.maxX);
            result.maxY = Math.max(maxY, rect.maxY);
        }
        return result;
    }

    /**
     * Get the union of this rect and the rect given
     * @param rect
     * @return this
     * @throws NullPointerException if rect was null
     */
    public Rect union(Rect rect) throws NullPointerException {
        return union(rect, this);
    }

    /**
     * Get the union of this rect and the vect given
     * @param vect
     * @return this
     * @throws NullPointerException if vect was null
     */
    public Rect union(Vect vect) throws NullPointerException {
        return unionInternal(vect.getX(), vect.getY());
    }

    /**
     * Get the union of this rect and the vector given
     * @param x
     * @param y
     * @return this
     * @throws IllegalArgumentException if x or y was infinite or NaN
     */
    public Rect union(double x, double y) throws IllegalArgumentException {
        Vect.check(x, y);
        return unionInternal(x, y);
    }

    Rect unionInternal(double x, double y) {
        minX = Math.min(minX, x);
        minY = Math.min(minY, y);
        maxX = Math.max(maxX, x);
        maxY = Math.max(maxY, y);
        return this;
    }

    /**
     * Get the unoin of this rect and the range of ordinates given
     * @param ords
     * @param startOffset start offset within array
     * @param endOffset end offset within array
     * @return this
     * @throws IndexOutOfBoundsException if startOffset or endOffset was out of bounds
     * @throws IllegalArgumentException if startOffset - endOffset is an odd (not even) number.
     */
    public Rect unionAll(double[] ords, int startOffset, int endOffset) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (endOffset < startOffset) {
            int tmp = startOffset;
            startOffset = endOffset;
            endOffset = tmp;
        }
        if (startOffset < 0) {
            throw new IndexOutOfBoundsException("startOffset out of array : " + startOffset);
        }
        if (endOffset > ords.length) {
            throw new IndexOutOfBoundsException("endOffset out of array : " + endOffset);
        }
        int len = endOffset - startOffset;
        if ((len & 1) == 1) {
            throw new IllegalArgumentException("Invalid length : " + len);
        }
        for (int i = startOffset; i < endOffset; i++) {
            if (!Util.isValid(ords[i])) {
                throw new IllegalArgumentException("Invalid ordinate (" + ords[i] + ") at index " + i);
            }
        }
        boolean valid = isValid();
        while (startOffset < endOffset) {
            double x = ords[startOffset++];
            double y = ords[startOffset++];
            if (valid) {
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            } else {
                minX = maxX = x;
                minY = maxY = y;
                valid = true;
            }
        }
        return this;
    }

    /**
     * Get a Rect based on this buffered by the amount given
     *
     * @param amt
     * @return this
     * @throws IllegalArgumentException if amt was infinite or NaN
     * @throws IllegalStateException if invalid
     */
    public Rect buffer(double amt) throws IllegalArgumentException, IllegalStateException {
        Util.check(amt, "Invalid buffer amount {0}");
        if (!isValid()) {
            throw new IllegalStateException("Invalid rect!");
        }
        if (amt == 0) {
            return this;
        }
        if (amt < 0) {
            if (((minX - amt) > (maxX + amt)) || ((minY - amt) > (maxY + amt))) {
                reset();
                return this;
            }
        }
        minX -= amt;
        minY -= amt;
        maxX += amt;
        maxY += amt;
        return this;
    }

    @Override
    public Rect getBounds(Rect target) {
        return target.set(this);
    }

    @Override
    public PathIterator getPathIterator() {
        return getPathIterator(null);
    }

    @Override
    public PathIterator getPathIterator(final AffineTransform transform) {
        return new PathIterator(){
            int index = 0;
            
            @Override
            public int getWindingRule() {
                return WIND_EVEN_ODD;
            }

            @Override
            public boolean isDone() {
                return index < 4;
            }

            @Override
            public void next() {
                index++;
            }

            @Override
            public int currentSegment(float[] coords) {
                switch(index){
                    case 0:
                        coords[0] = (float)minX;
                        coords[1] = (float)minY;
                        transform(coords);
                        return SEG_MOVETO;
                    case 1:
                        coords[0] = (float)maxX;
                        coords[1] = (float)minY;
                        transform(coords);
                        return SEG_LINETO;
                    case 2:
                        coords[0] = (float)maxX;
                        coords[1] = (float)maxY;
                        transform(coords);
                        return SEG_LINETO;
                    case 3:
                        coords[0] = (float)minX;
                        coords[1] = (float)maxY;
                        transform(coords);
                        return SEG_LINETO;
                    case 4:
                        coords[0] = (float)minX;
                        coords[1] = (float)minY;
                        transform(coords);
                        return SEG_CLOSE;
                    default:
                        throw new IllegalStateException();
                }
            }

            @Override
            public int currentSegment(double[] coords) {
                switch(index){
                    case 0:
                        coords[0] = minX;
                        coords[1] = minY;
                        transform(coords);
                        return SEG_MOVETO;
                    case 1:
                        coords[0] = maxX;
                        coords[1] = minY;
                        transform(coords);
                        return SEG_LINETO;
                    case 2:
                        coords[0] = maxX;
                        coords[1] = maxY;
                        transform(coords);
                        return SEG_LINETO;
                    case 3:
                        coords[0] = minX;
                        coords[1] = maxY;
                        transform(coords);
                        return SEG_LINETO;
                    case 4:
                        coords[0] = minX;
                        coords[1] = minY;
                        transform(coords);
                        return SEG_CLOSE;
                    default:
                        throw new IllegalStateException();
                }
            }
            
            void transform(double[] coords){
                if(transform != null){
                    transform.transform(coords, 0, coords, 0, 1);
                }
            }
            
            void transform(float[] coords){
                if(transform != null){
                    transform.transform(coords, 0, coords, 0, 1);
                }
            }
                
        };
    }

    @Override
    public PathIterator getPathIterator(AffineTransform transform, double flatness) {
        return getPathIterator(null);
    }

    
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Util.hash(minX);
        hash = 43 * hash + Util.hash(minY);
        hash = 43 * hash + Util.hash(maxX);
        hash = 43 * hash + Util.hash(maxY);
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
        return "[" + Util.ordToStr(minX) + ',' + Util.ordToStr(minY) + ','
                + Util.ordToStr(maxX) + ',' + Util.ordToStr(maxY) + ']';
    }

    /**
     * Convert this rect to a string in the format [minX,minY,maxX,maxY] and add it to
     * the appendable given
     *
     * @param appendable
     * @throws IOException if there was an output error
     * @throws NullPointerException if appendable was null
     */
    public void toString(Appendable appendable) throws IOException {
        appendable.append('[').append(Util.ordToStr(minX)).append(',')
                .append(Util.ordToStr(minY)).append(',')
                .append(Util.ordToStr(maxX)).append(',')
                .append(Util.ordToStr(maxY)).append(']');
    }

    @Override
    public Rect clone() throws CloneNotSupportedException {
        return (Rect) super.clone();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        writeData(out);
    }

    /**
     * Write this rect to the output given
     *
     * @param out
     * @throws IOException if out was null
     * @throws NullPointerException if out was null
     */
    public void writeData(DataOutput out) throws IOException, NullPointerException {
        out.writeDouble(minX);
        out.writeDouble(minY);
        out.writeDouble(maxX);
        out.writeDouble(maxY);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        readData(in);
    }

    /**
     * Set the ordinates for this line from the input given
     * @param in
     * @return this
     * @throws IOException if there was an error
     * @throws NullPointerException if in was null
     */
    public Rect readData(DataInput in) throws IOException, NullPointerException {
        return set(in.readDouble(), in.readDouble(),
                in.readDouble(), in.readDouble());
    }

    /**
     * Read a line from the input given
     * @param in
     * @return a line
     * @throws IOException if there was an error
     * @throws NullPointerException if in was null
     */
    public static Rect read(DataInput in) throws IOException, NullPointerException {
        return new Rect().readData(in);
    }

}
