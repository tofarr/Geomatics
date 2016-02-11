package org.jg.geom;

import java.io.Serializable;

/**
 *
 * @author tofar_000
 */
public final class VectBuilder implements Cloneable, Serializable {

    private double x;
    private double y;

    public VectBuilder() {

    }

    public VectBuilder(double x, double y) throws IllegalArgumentException {
        set(x, y);
    }

    public VectBuilder set(double x, double y) throws IllegalArgumentException {
        Vect.check(x, y);
        this.x = x;
        this.y = y;
        return this;
    }

    public VectBuilder set(Vect vect) throws IllegalArgumentException {
        this.x = vect.x;
        this.y = vect.y;
        return this;
    }

    public VectBuilder set(VectBuilder vect) throws IllegalArgumentException {
        this.x = vect.x;
        this.y = vect.y;
        return this;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        Vect.check(x, "Invalid x : {0}");
        this.x = x;
    }

    public double getY() {
        return y;
    }

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

    @Override
    public String toString() {
        return Vect.toString(x, y);
    }

    @Override
    protected VectBuilder clone() {
        return new VectBuilder(x, y);
    }

}
