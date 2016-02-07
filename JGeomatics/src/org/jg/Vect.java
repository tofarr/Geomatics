package org.jg;

import java.beans.ConstructorProperties;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * 2D vector, where ordinates can never be Infinite or NaN
 *
 * @author tim.ofarrell
 */
public final class Vect implements Cloneable, Comparable<Vect>, Externalizable {

    double x;
    double y;

    /**
     * Create a new vect set to (0,0)
     */
    public Vect() {
    }

    /**
     * Create a new vect
     *
     * @param x
     * @param y
     * @throws IllegalArgumentException if x or y was infinite or NaN
     */
    @ConstructorProperties({"x", "y"})
    public Vect(double x, double y) throws IllegalArgumentException {
        set(x, y);
    }

    /**
     * Set this vector to the value given
     *
     * @param x
     * @param y
     * @return this
     * @throws IllegalArgumentException if x or y was Infinite or NaN
     */
    public Vect set(double x, double y) throws IllegalArgumentException {
        check(x, y);
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * Check the ordinate given
     *
     * @param x
     * @param y
     * @throws IllegalArgumentException if x or y was infinite or NaN
     */
    static void check(double x, double y) throws IllegalArgumentException {
        Util.check(x, "Invalid x : {0}");
        Util.check(y, "Invalid y : {0}");
    }

    /**
     * Create a new vect as a copy of that given
     *
     * @param vect
     */
    public Vect(Vect vect) throws NullPointerException {
        set(vect);
    }

    /**
     * Set the values in this vector to those given
     *
     * @param vect
     * @return this
     * @throws NullPointerException if vect was null
     */
    public Vect set(Vect vect) throws NullPointerException {
        this.x = vect.x;
        this.y = vect.y;
        return this;
    }

    /**
     * Get x
     *
     * @return
     */
    public double getX() {
        return x;
    }

    /**
     * Set x
     *
     * @param x
     * @throws IllegalArgumentException if x was infinite or NaN
     */
    public void setX(double x) throws IllegalArgumentException {
        Util.check(x, "Invalid x : {0}");
        this.x = x;
    }

    /**
     * Get y
     *
     * @return
     */
    public double getY() {
        return y;
    }

    /**
     * Set y
     *
     * @param y
     * @throws IllegalArgumentException if y was infinite or NaN
     */
    public void setY(double y) throws IllegalArgumentException {
        Util.check(y, "Invalid y : {0}");
        this.y = y;
    }

    /**
     * Add the vector given to this
     *
     * @param vect
     * @return this
     * @throws NullPointerException if vect was null
     */
    public Vect add(Vect vect) throws NullPointerException {
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
    public Vect add(double x, double y) throws IllegalArgumentException {
        check(x, y);
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
    public Vect sub(Vect vect) throws NullPointerException {
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
    public Vect sub(double x, double y) {
        check(x, y);
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
    public Vect mul(double scalar) {
        Util.check(scalar, "Invalid scalar : {0}");
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
    public Vect div(double scalar) throws IllegalArgumentException {
        if (scalar == 0) {
            throw new IllegalArgumentException("Invalid scalar : " + scalar);
        }
        Util.check(scalar, "Invalid scalar : {0}");
        x /= scalar;
        y /= scalar;
        return this;
    }

    /**
     * Get the distance of this vector from the origin
     *
     * @return
     */
    public double length() {
        return Math.sqrt(lengthSq());
    }

    /**
     * Get the square of the distance of this vector from the origin
     *
     * @return
     */
    public double lengthSq() {
        double ret = (x * x) + (y * y);
        return ret;
    }

    /**
     * Get the distance between this vector and the vector given
     *
     * @param vect
     * @return
     */
    public double dist(Vect vect) throws NullPointerException {
        return Math.sqrt(distSq(vect));
    }

    /**
     * Get the square of the distance between this vector and the vector given
     *
     * @param vect
     * @return
     */
    public double distSq(Vect vect) throws NullPointerException {
        return distSq(x, y, vect.x, vect.y);
    }

    /**
     * Get the square of the distance between the 2 points given
     *
     * @param ax
     * @param ay
     * @param bx
     * @param by
     * @return distance squared
     */
    static double distSq(double ax, double ay, double bx, double by) {
        double dx = bx - ax;
        double dy = by - ay;
        double ret = (dx * dx) + (dy * dy);
        return ret;
    }

    /**
     * Get the dydx of this vector
     *
     * @return
     */
    public double dydx() {
        return y / x;
    }

    /**
     * Get the dydx of the vector from this vector to the vector given
     *
     * @param vect
     * @return
     * @throws NullPointerException if vect was null
     */
    public double dydxTo(Vect vect) throws NullPointerException {
        return dydxTo(x, y, vect.x, vect.y);
    }

    /**
     * Get the dydx of the vector from ax,ay to bx,by
     *
     * @return
     */
    static double dydxTo(double ax, double ay, double bx, double by) {
        double dx = bx - ax;
        double dy = by - ay;
        double ret = dy / dx;
        return ret;
    }

    /**
     * Get the direction in radians from the origin to this vector
     *
     * @return
     */
    public double directionInRadians() {
        return directionInRadians(x, y);
    }

    /**
     * Get the direction in radians of the vector given
     *
     * @return
     * @throws IllegalArgumentException
     */
    static double directionInRadians(double x, double y) throws IllegalArgumentException {
        if ((x == 0) && (y == 0)) {
            throw new IllegalArgumentException("Non directional vector!");
        }
        double ret = Math.atan2(y, x);
        if (ret < 0) {
            ret = (Math.PI * 2) + ret;
        }
        return ret;
    }

    /**
     * Get the direction in radians from this vector to the vector given
     *
     * @param vect
     * @return
     * @throws NullPointerException if vect was null
     * @throws IllegalArgumentException if the vectors are the same
     */
    public double directionInRadiansTo(Vect vect) throws NullPointerException, IllegalArgumentException {
        return directionInRadiansTo(x, y, vect.x, vect.y);
    }

    /**
     * Get the direction in radians from ax,ay to bx,by
     *
     * @param ax
     * @param ay
     * @param bx
     * @param by
     * @return
     */
    static double directionInRadiansTo(double ax, double ay, double bx, double by) {
        return directionInRadians(bx - ax, by - ay);
    }

    /**
     * Get the dot product of this vector and the vector given
     *
     * @param vect
     * @return
     * @throws NullPointerException if vect was null
     */
    public double dot(Vect vect) throws NullPointerException {
        return dot(x, y, vect.x, vect.y);
    }

    /**
     * Get the dot product of the vectors given
     *
     * @param ax
     * @param ay
     * @param bx
     * @param by
     * @return
     */
    static double dot(double ax, double ay, double bx, double by) {
        double ret = (ax * bx) + (ay * by);
        return ret;
    }

    /**
     * Determine if this vect matches a Vect / VectBuilder given, within the
     * tolerance given
     *
     * @param vect
     * @param tolerance
     * @return
     * @throws NullPointerException if tolerance was null
     */
    public boolean match(Vect vect, Tolerance tolerance) throws NullPointerException {
        return tolerance.match(x, y, vect.x, vect.y);
    }

    /**
     * Compare the vectors ax,ay and bx,by. Returns -1 if ax < bx or ax == bx
     * and ay < by. Returns 0 if ax == bx and ay == by. Returns 1 otherwise.
     *
     * @param ax
     * @param ay
     * @param bx
     * @param by
     * @return
     */
    static int compare(double ax, double ay, double bx, double by) {
        if (ax < bx) {
            return -1;
        } else if (ax > bx) {
            return 1;
        } else if (ay < by) { // at this point ax == bx
            return -1;
        } else if (ay > by) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public int compareTo(Vect vect) {
        return compare(x, y, vect.x, vect.y);
    }

    @Override
    public String toString() {
        return toString(x, y);
    }

    /**
     * Get a string representation of this vector
     *
     * @param x
     * @param y
     * @return "[x,y]"
     */
    static String toString(double x, double y) {
        return '[' + Util.ordToStr(x) + ',' + Util.ordToStr(y) + ']';
    }

    /**
     * Append a string representation of this vect to the appendable given
     *
     * @param appendable
     * @throws IllegalStateException if there was an io error or appendable was
     * null
     */
    public void toString(Appendable appendable) throws IllegalStateException {
        try {
            appendable.append('[').append(Util.ordToStr(x)).append(',').append(Util.ordToStr(y)).append(']');
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public int hashCode() {
        return hashCode(x, y);
    }

    static int hashCode(double x, double y) {
        int hash = 5;
        hash = 47 * hash + Util.hash(x);
        hash = 47 * hash + Util.hash(y);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Vect) {
            Vect vect = (Vect) obj;
            return (x == vect.x) && (y == vect.y);
        }
        return false;
    }

    @Override
    public Vect clone() {
        return new Vect(x, y);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        writeData(out);
    }

    /**
     * Write this vector to the output given
     *
     * @param out
     * @throws IOException if out was null
     * @throws NullPointerException if out was null
     */
    public void writeData(DataOutput out) throws IOException, NullPointerException {
        out.writeDouble(x);
        out.writeDouble(y);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        readData(in);
    }

    /**
     * Set the ordinates for this vector from the input given
     *
     * @param in
     * @return this
     * @throws IOException if there was an error
     * @throws NullPointerException if in was null
     */
    public Vect readData(DataInput in) throws IOException, NullPointerException {
        return set(in.readDouble(), in.readDouble());
    }

    /**
     * Read a vector from the input given
     *
     * @param in
     * @return a line
     * @throws IOException if there was an error
     * @throws NullPointerException if in was null
     */
    public static Vect read(DataInput in) throws IOException, NullPointerException {
        return new Vect().readData(in);
    }

}
