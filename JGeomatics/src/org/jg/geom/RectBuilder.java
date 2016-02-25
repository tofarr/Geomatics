package org.jg.geom;

import java.beans.Transient;
import java.io.Serializable;

/**
 *
 * @author tofar_000
 */
public final class RectBuilder implements Cloneable, Serializable {

    private double minX;
    private double minY;
    private double maxX;
    private double maxY;
    private boolean valid;

    /**
     * Create a new instance of RectBuilder
     */
    public RectBuilder() {
        reset();
    }

    /**
     * Create a new instance of RectBuilder (ordinates are swapped if min > max)
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     * @throws IllegalArgumentException if an ordinate was infinite or NaN
     */
    public RectBuilder(double minX, double minY, double maxX, double maxY) throws IllegalArgumentException {
        set(minX, minY, maxX, maxY);
    }

    /**
     * Set the ordinates for this RectBuilder (ordinates are swapped if min > max)
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     * @return this
     * @throws IllegalArgumentException if an ordinate was infinite or NaN
     */
    public RectBuilder set(double minX, double minY, double maxX, double maxY) throws IllegalArgumentException {
        Rect.check(minX, minY, maxX, maxY);
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
        return setInternal(minX, minY, maxX, maxY);
    }

    /**
     * Set the ordinates for this RectBuilder
     * @param rect
     * @return this
     * @throws NullPointerException if rect was null
     */
    public RectBuilder set(Rect rect) throws NullPointerException {
        return setInternal(rect.minX, rect.minY, rect.maxX, rect.maxY);
    }

    /**
     * Set the ordinates for this RectBuilder
     * @param rect
     * @return this
     * @throws NullPointerException if rect was null
     */
    public RectBuilder set(RectBuilder rect) throws NullPointerException {
        setInternal(rect.minX, rect.minY, rect.maxX, rect.maxY);
        this.valid = rect.valid;
        return this;
    }

    RectBuilder setInternal(double minX, double minY, double maxX, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.valid = true;
        return this;
    }

    /**
     * Reset this rect builder to an invalid state [NaN,NaN,NaN,NaN]
     * @return this
     */
    public RectBuilder reset() {
        valid = false;
        minX = minY = maxX = maxY = Double.NaN;
        return this;
    }

    /**
     * Add the vector given to this rectangle
     * @param x
     * @param y
     * @return this
     * @throws IllegalArgumentException if x or y was infinite or NaN
     */
    public RectBuilder add(double x, double y) throws IllegalArgumentException {
        Vect.check(x, y);
        return addInternal(x, y);
    }
    
    /**
     * Add the vector given to this rectangle
     * @param vect
     * @return this
     * @throws NullPointerException if vect was null
     */
    public RectBuilder add(VectBuilder vect) throws NullPointerException {
        return addInternal(vect.getX(), vect.getY());
    }
    
    /**
     * Add the vector given to this rectangle
     * @param vect
     * @return this
     * @throws NullPointerException if vect was null
     */
    public RectBuilder add(Vect vect) throws NullPointerException {
        return addInternal(vect.x, vect.y);
    }

    /**
     * Add the rectangle given to this rectangle
     * @param rect
     * @return this
     * @throws NullPointerException if rect was null
     */
    public RectBuilder add(Rect rect) throws NullPointerException {
        addInternal(rect.minX, rect.minY);
        return addInternal(rect.maxX, rect.maxY);
    }


    /**
     * Add the rectangle given to this rectangle
     * @param rect
     * @return this
     * @throws NullPointerException if rect was null
     */
    public RectBuilder add(RectBuilder rect) throws NullPointerException {
        if (rect.isValid()) {
            addInternal(rect.minX, rect.minY);
            addInternal(rect.maxX, rect.maxY);
        }
        return this;
    }

    RectBuilder addInternal(double x, double y) {
        if (valid) {
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        } else {
            minX = maxX = x;
            minY = maxY = y;
            valid = true;
        }
        return this;
    }

    public RectBuilder addAll(double[] ords, int startOffset, int numVects) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        int endOffset = (numVects << 1) + startOffset;
        for (int i = startOffset; i < endOffset;) {
            Vect.check(ords[i++], ords[i++]);
        }
        for (int i = startOffset; i < endOffset;) {
            addInternal(ords[i++], ords[i++]);
        }
        return this;
    }

    public RectBuilder addRects(Rect... rects) throws NullPointerException {
        return addRects(rects, 0, rects.length);
    }

    public RectBuilder addRects(Rect[] rects, int startOffset, int numRects) throws NullPointerException, IndexOutOfBoundsException {
        int endOffset = startOffset + numRects;
        for (int i = startOffset; i < endOffset; i++) {
            if (rects[i] == null) {
                throw new NullPointerException("Null rect at index : " + i);
            }
        }
        for (int i = startOffset; i < endOffset; i++) {
            add(rects[i]);
        }
        return this;
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public boolean isValid() {
        return valid;
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
     * Get a Rect based on this buffered by the amount given
     *
     * @param amt
     * @return this
     * @throws IllegalArgumentException if amt was infinite or NaN
     * @throws IllegalStateException if rect was invalid
     */
    public RectBuilder buffer(double amt) throws IllegalArgumentException, IllegalStateException {
        Vect.check(amt, "Invalid buffer amount {0}");
        if(!isValid()){
            throw new IllegalStateException("Invalid rectangle");
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

    //produces null
    public Rect build() {
        return valid ? new Rect(minX, minY, maxX, maxY) : null;
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
        if (obj instanceof RectBuilder) {
            RectBuilder rect = (RectBuilder) obj;
            if(valid){
                return (minX == rect.minX)
                        && (minY == rect.minY)
                        && (maxX == rect.maxX)
                        && (maxY == rect.maxY);
            }else{
                return !rect.valid;
            }
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return Rect.toString(minX, minY, maxX, maxY);
    }

    @Override
    public RectBuilder clone() {
        return valid ? new RectBuilder(minX, minY, maxX, maxY) : new RectBuilder();
    }

}
