package org.jg.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import org.jg.geom.GeomException;
import org.jg.geom.Rect;
import org.jg.geom.Vect;

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
    
    public View resizeTo(int widthPx, int heightPx){
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
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("{bounds:");
        Rect.toString(bounds, str);
        str.append(",widthPx:").append(widthPx).append(",heightPx:").append(heightPx).append('}');
        return str.toString();
    }

    @Override
    public View clone() {
        return this;
    }
     
    /**
     * Read a View from to the DataInput given
     *
     * @param in
     * @return a VectList
     * @throws NullPointerException if in was null
     * @throws IllegalArgumentException if the stream contained infinite or NaN ordinates
     * @throws GeomException if there was an IO error
     */
    public static View read(DataInput in) throws NullPointerException, IllegalArgumentException, GeomException{
        try {
            Rect bounds = Rect.read(in);
            int widthPx = in.readInt();
            int heightPx = in.readInt();
            return new View(bounds, widthPx, heightPx);
        } catch (IOException ex) {
            throw new GeomException("Error reading VectList", ex);
        }
    }
    
    /**
     * Write this View to the DataOutput given
     *
     * @param out
     * @throws NullPointerException if out was null
     * @throws GeomException if there was an IO error
     */
    public void write(DataOutput out) throws NullPointerException, GeomException{
        try {
            bounds.write(out);
            out.writeInt(widthPx);
            out.writeInt(heightPx);
        } catch (IOException ex) {
            throw new GeomException("Error writing VectList", ex);
        }
    }
   
}
