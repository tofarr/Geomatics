package org.jg.geom;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

/**
 * Mutable vector - may be used to build immutable vectors
 *
 * @author tofar_000
 */
public final class VectBuilder implements Cloneable, Serializable, Comparable<VectBuilder> {

    private double x;
    private double y;

    /**
     * Create a new instance of VectBuilder set to [0,0]
     */
    public VectBuilder() {
    }

    /**
     * Create a new instance of VectBuilder set to the value given
     *
     * @param vect
     * @throws NullPointerException if vect was null
     */
    public VectBuilder(Vect vect) throws NullPointerException {
        set(vect.x, vect.y);
    }

    /**
     * Create a new instance of VectBuilder set to the value given
     *
     * @param vect
     * @throws NullPointerException if vect was null
     */
    public VectBuilder(VectBuilder vect) throws NullPointerException {
        set(vect.x, vect.y);
    }

    /**
     * Create a new instance of VectBuilder set to the value given
     *
     * @param x
     * @param y
     * @throws IllegalArgumentException if x or y was infinite or NaN
     */
    public VectBuilder(double x, double y) throws IllegalArgumentException {
        set(x, y);
    }

    /**
     * Set this vector to the value given
     *
     * @param x
     * @param y
     * @return this
     * @throws IllegalArgumentException if x or y was infinite or NaN
     */
    public VectBuilder set(double x, double y) throws IllegalArgumentException {
        Vect.check(x, y);
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * Set this vector to the value given
     *
     * @param vect
     * @return this
     * @throws NullPointerException if vect was null
     */
    public VectBuilder set(Vect vect) throws NullPointerException {
        this.x = vect.x;
        this.y = vect.y;
        return this;
    }

    /**
     * Set this vector to the value given
     *
     * @param vect
     * @return this
     * @throws NullPointerException if vect was null
     */
    public VectBuilder set(VectBuilder vect) throws NullPointerException {
        this.x = vect.x;
        this.y = vect.y;
        return this;
    }

    /**
     * Get the x value
     *
     * @return
     */
    public double getX() {
        return x;
    }

    /**
     * Set the x value
     *
     * @param x
     * @throws IllegalArgumentException if x was infinite or NaN
     */
    public void setX(double x) throws IllegalArgumentException {
        Vect.check(x, "Invalid x : {0}");
        this.x = x;
    }

    /**
     * Get the y value
     *
     * @return
     */
    public double getY() {
        return y;
    }

    /**
     * Set the y value
     *
     * @param y
     * @throws IllegalArgumentException if x was infinite or NaN
     */
    public void setY(double y) {
        Vect.check(y, "Invalid y : {0}");
        this.y = y;
    }

    /**
     * Add the vector given to this
     *
     * @param vect
     * @return this
     * @throws NullPointerException if vect was null
     */
    public VectBuilder add(Vect vect) throws NullPointerException {
        this.x += vect.x;
        this.y += vect.y;
        return this;
    }

    /**
     * Add the vector given to this
     *
     * @param vect
     * @return this
     * @throws NullPointerException if vect was null
     */
    public VectBuilder add(VectBuilder vect) throws NullPointerException {
        this.x += vect.x;
        this.y += vect.y;
        return this;
    }

    /**
     * Add the vector given to this
     *
     * @param x
     * @param y
     * @return this
     * @throws IllegalArgumentException if x or y was was infinite or NaN
     */
    public VectBuilder add(double x, double y) throws IllegalArgumentException {
        Vect.check(x, y);
        this.x += x;
        this.y += y;
        return this;
    }

    /**
     * Subtract the vector given from this
     *
     * @param vect
     * @return this
     * @throws NullPointerException if vect was null
     */
    public VectBuilder sub(Vect vect) throws NullPointerException {
        this.x -= vect.x;
        this.y -= vect.y;
        return this;
    }

    /**
     * Subtract the vector given from this
     *
     * @param vect
     * @return this
     * @throws NullPointerException if vect was null
     */
    public VectBuilder sub(VectBuilder vect) throws NullPointerException {
        this.x -= vect.x;
        this.y -= vect.y;
        return this;
    }

    /**
     * Subtract the vector given from this
     *
     * @param x
     * @param y
     * @return this
     * @throws IllegalArgumentException if x or y was was infinite or NaN
     */
    public VectBuilder sub(double x, double y) throws IllegalArgumentException {
        Vect.check(x, y);
        this.x -= x;
        this.y -= y;
        return this;
    }

    /**
     * Multiply this by the scalar given
     *
     * @param scalar
     * @return this
     * @throws IllegalArgumentException if scalar was infinite or NaN
     */
    public VectBuilder mul(double scalar) throws IllegalArgumentException {
        Vect.check(scalar, "Invalid scalar : {0}");
        x *= scalar;
        y *= scalar;
        return this;
    }

    /**
     * Divide this by the scalar given
     *
     * @param scalar
     * @return this
     * @throws IllegalArgumentException if scalar was infinite, NaN or 0
     */
    public VectBuilder div(double scalar) throws IllegalArgumentException {
        if (scalar == 0) {
            throw new IllegalArgumentException("Invalid scalar : " + scalar);
        }
        Vect.check(scalar, "Invalid scalar : {0}");
        x /= scalar;
        y /= scalar;
        return this;
    }
    
    /**
     * Create a vect based on this builder
     * @return a vect based on this builder
     */
    public Vect build(){
        return (x == 0) && (y == 0) ? Vect.ZERO : new Vect(x, y);
    }

    /**
     * Write this vect to the DataOutput given
     *
     * @param out
     * @throws NullPointerException if out was null
     * @throws GeomException if there was an IO error
     */
    public void write(DataOutput out) throws NullPointerException, GeomException {
        Vect.write(x, y, out);
    }
    
    /**
     * Read a vector from to the DataInput given
     *
     * @param in
     * @return a vector
     * @throws NullPointerException if in was null
     * @throws IllegalArgumentException if the stream contained infinite or NaN ordinates
     * @throws GeomException if there was an IO error
     */
    public static VectBuilder read(DataInput in) throws NullPointerException, IllegalArgumentException, 
        GeomException {
        try {
            return new VectBuilder(in.readDouble(), in.readDouble());
        } catch (IOException ex) {
            throw new GeomException("Error reading vector", ex);
        }
    }
    
    @Override
    public int compareTo(VectBuilder vect) {
        return Vect.compare(x, y, vect.x, vect.y);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VectBuilder) {
            VectBuilder vect = (VectBuilder) obj;
            return (x == vect.x) && (y == vect.y);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Vect.hashCode(x, y);
    }

    @Override
    public String toString() {
        return Vect.toString(x, y);
    }

    @Override
    protected VectBuilder clone() {
        return new VectBuilder(x, y);
    }

}
