package org.geomatics.util;

import java.beans.Transient;
import java.io.Serializable;
import org.geomatics.geom.Rect;
import org.geomatics.geom.Vect;

/**
 * Immutable view object.
 *
 * @author tim.ofarrell
 */
public class View implements Serializable, Cloneable {

    final Rect bounds;
    final Vect center;
    final double resolutionX;
    final double resolutionY;

    final int widthPx;
    final int heightPx;

    final Transform transform;

    /**
     * Create a new view with the bounds and pixel sizes given
     *
     * @param bounds
     * @param widthPx
     * @param heightPx
     * @throws NullPointerException
     * @throws IllegalArgumentException if widthPx or heightPx <= 0
     */
    public View(Rect bounds, int widthPx, int heightPx) throws NullPointerException, IllegalArgumentException {
        if (widthPx <= 0) {
            throw new IllegalArgumentException("Invalid widthPx : " + widthPx);
        }
        if (heightPx <= 0) {
            throw new IllegalArgumentException("Invalid heightPx : " + heightPx);
        }
        this.bounds = bounds;
        this.widthPx = widthPx;
        this.heightPx = heightPx;
        this.center = bounds.getCentroid();
        this.resolutionX = bounds.getWidth() / widthPx;
        this.resolutionY = bounds.getHeight() / heightPx;
        this.transform = buildTransform(bounds, resolutionX, resolutionY);
    }

    public View(Vect center, double resolution, int widthPx, int heightPx) throws NullPointerException, IllegalArgumentException {
        this(center, resolution, resolution, widthPx, heightPx);
    }

    public View(Vect center, double resolutionX, double resolutionY, int widthPx, int heightPx) throws NullPointerException, IllegalArgumentException {
        Vect.check(resolutionX, "Invalid resolutionX : {0}");
        if (resolutionX <= 0) {
            throw new IllegalArgumentException("Invalid resolutionX : " + resolutionX);
        }
        Vect.check(resolutionY, "Invalid resolutionY : {0}");
        if (resolutionY <= 0) {
            throw new IllegalArgumentException("Invalid resolutionY : " + resolutionY);
        }
        if (widthPx <= 0) {
            throw new IllegalArgumentException("Invalid widthPx : " + widthPx);
        }
        if (heightPx <= 0) {
            throw new IllegalArgumentException("Invalid heightPx : " + heightPx);
        }
        double width = widthPx * resolutionX;
        double height = heightPx * resolutionY;
        double minX = center.getX() - (width / 2);
        double minY = center.getY() - (height / 2);
        double maxX = minX + width;
        double maxY = minY + height;
        this.bounds = Rect.valueOf(minX, minY, maxX, maxY);
        this.widthPx = widthPx;
        this.heightPx = heightPx;
        this.center = center;
        this.resolutionX = resolutionX;
        this.resolutionY = resolutionY;
        this.transform = buildTransform(bounds, resolutionX, resolutionY);
    }

    public Rect getBounds() {
        return bounds;
    }

    public Vect getCenter() {
        return center;
    }

    public double getResolutionX() {
        return resolutionX;
    }

    public double getResolutionY() {
        return resolutionY;
    }

    @Transient
    public double getResolution() {
        return Math.max(resolutionX, resolutionY);
    }

    public int getWidthPx() {
        return widthPx;
    }

    public int getHeightPx() {
        return heightPx;
    }

    static Transform buildTransform(Rect bounds, double resolutionX, double resolutionY) {
        return new TransformBuilder().translate(-bounds.minX, -bounds.maxY).scale(1 / resolutionX, -1 / resolutionY).build();
    }

    public Transform getTransform() {
        return transform;
    }

    public View zoom(double amt) {
        return new View(center, resolutionX * amt, resolutionY * amt, widthPx, heightPx);
    }

    public View zoomTo(double resolution) {
        return new View(center, resolution, resolution, widthPx, heightPx);
    }

    public View panTo(Vect center) {
        return new View(center, resolutionX, resolutionY, widthPx, heightPx);
    }

    public View resizeTo(int widthPx, int heightPx) {
        return new View(center, resolutionX, resolutionY, widthPx, heightPx);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + this.bounds.hashCode();
        hash = 79 * hash + this.widthPx;
        hash = 79 * hash + this.heightPx;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof View) {
            View other = (View) obj;
            return this.bounds.equals(other.bounds)
                    && (this.widthPx == other.widthPx)
                    && (this.heightPx == other.heightPx);
        }
        return false;
    }

    @Override
    public View clone() {
        return this;
    }
}
