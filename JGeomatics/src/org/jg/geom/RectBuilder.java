package org.jg.geom;

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
    private boolean empty;

    public RectBuilder() {
        empty = true;
    }

    public RectBuilder(double minX, double minY, double maxX, double maxY) throws IllegalArgumentException {
        set(minX, minY, maxX, maxY);
    }

    public RectBuilder set(double minX, double minY, double maxX, double maxY) throws IllegalArgumentException {
        Rect.check(minX, minY, maxX, maxY);
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.empty = false;
        return this;
    }

    public RectBuilder reset() {
        empty = true;
        return this;
    }

    public RectBuilder add(double x, double y) throws IllegalArgumentException {
        Vect.check(x, y);
        return addInternal(x, y);
    }

    public RectBuilder add(VectBuilder vect) throws NullPointerException {
        return addInternal(vect.getX(), vect.getY());
    }

    public RectBuilder add(Vect vect) throws NullPointerException {
        return addInternal(vect.x, vect.y);
    }

    public RectBuilder add(Rect rect) throws NullPointerException {
        addInternal(rect.minX, rect.minY);
        return addInternal(rect.maxX, rect.maxY);
    }

    public RectBuilder add(RectBuilder rect) throws NullPointerException {
        if (!rect.isEmpty()) {
            addInternal(rect.minX, rect.minY);
            addInternal(rect.maxX, rect.maxY);
        }
        return this;
    }

    RectBuilder addInternal(double x, double y) {
        if (empty) {
            minX = maxX = x;
            minY = maxY = y;
        } else {
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
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

    public boolean isEmpty() {
        return empty;
    }
    
    
    /**
     * Get a Rect based on this buffered by the amount given
     *
     * @param amt
     * @return this
     * @throws IllegalArgumentException if amt was infinite or NaN
     */
    public RectBuilder buffer(double amt) throws IllegalArgumentException {
        Vect.check(amt, "Invalid buffer amount {0}");
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
        return empty ? null : new Rect(minX, minY, maxX, maxY);
    }

    @Override
    public String toString() {
        return Rect.toString(minX, minY, maxX, maxY);
    }

    @Override
    protected RectBuilder clone() {
        return empty ? new RectBuilder() : new RectBuilder(minX, minY, maxX, maxY);
    }
    
}
