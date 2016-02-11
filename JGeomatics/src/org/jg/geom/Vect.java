package org.jg.geom;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.text.MessageFormat;
import org.jg.Network;
import org.jg.util.Tolerance;

/**
 * Immutable vector
 *
 * @author tofar_000
 */
public final class Vect implements Geom, Comparable<Vect> {

    public static final Vect ZERO = new Vect(0, 0);
    public final double x;
    public final double y;

    Vect(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Vect valueOf(double x, double y) throws IllegalArgumentException {
        check(x, y);
        if ((x == 0) && (y == 0)) {
            return ZERO;
        }
        return new Vect(x, y);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public VectBuilder toBuilder() {
        return new VectBuilder(x, y);
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
     * Get the direction in radians from the origin to this vector
     *
     * @return
     */
    public double directionInRadians() {
        return directionInRadians(x, y);
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

    @Override
    public Rect getBounds() {
        return new Rect(x, y, 0, 0);
    }

    @Override
    public void addBoundsTo(RectBuilder target) throws NullPointerException {
        target.add(x, y);
    }

    @Override
    public Geom transform(Transform transform) throws NullPointerException {
        return transform.transform(this);
    }

    @Override
    public PathIterator pathIterator() {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(x, y);
        path.lineTo(x, y);
        return path.getPathIterator(null);
    }

    @Override
    public void addTo(Network network, double flatness) throws NullPointerException, IllegalArgumentException {
        network.addVertex(this);
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
    public static int compare(double ax, double ay, double bx, double by) {
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
    public int hashCode() {
        return hashCode(x, y);
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
        return this;
    }

    @Override
    public String toString() {
        return toString(x, y);
    }

    @Override
    public void toString(Appendable appendable) throws NullPointerException, GeomException {
        toString(x, y, appendable);
    }

    public static void check(double x, double y) throws IllegalArgumentException {
        check(x, "Invalid x : {0}");
        check(y, "Invalid y : {0}");
    }

    public static void check(double ord, String pattern) throws IllegalArgumentException {
        if (Double.isInfinite(ord) || Double.isNaN(ord)) {
            throw new IllegalArgumentException(MessageFormat.format(pattern, Double.toString(ord)));
        }
    }

    public static String ordToStr(double ord) {
        String ret = Double.toString(ord);
        if (ret.endsWith(".0")) {
            ret = ret.substring(0, ret.length() - 2);
        }
        return ret;
    }

    public static int hash(double value) {
        long val = Double.doubleToLongBits(value);
        int ret = (int) (val ^ (val >>> 32));
        //we do a bitwise shift here, as it serves to effectively randomise the number
        //and helps prevent clustering in hashing algorithms, massively boosting performance
        for (int i = 0; i < 3; i++) {
            int ret2 = (ret >>> 8);
            ret ^= ret2;
        }
        return ret;
    }

    public static int hashCode(double x, double y) {
        int hash = 5;
        hash = 47 * hash + hash(x);
        hash = 47 * hash + hash(y);
        return hash;
    }

    public static double dot(double ax, double ay, double bx, double by) {
        double ret = (ax * bx) + (ay * by);
        return ret;
    }

    public static double distSq(double ax, double ay, double bx, double by) {
        double dx = bx - ax;
        double dy = by - ay;
        double ret = (dx * dx) + (dy * dy);
        return ret;
    }

    public static double dydxTo(double ax, double ay, double bx, double by) {
        double dx = bx - ax;
        double dy = by - ay;
        double ret = dy / dx;
        return ret;
    }

    public static double directionInRadians(double x, double y) throws IllegalArgumentException {
        if ((x == 0) && (y == 0)) {
            throw new IllegalArgumentException("Non directional vector!");
        }
        double ret = Math.atan2(y, x);
        if (ret < 0) {
            ret = (Math.PI * 2) + ret;
        }
        return ret;
    }

    public static double directionInRadiansTo(double ax, double ay, double bx, double by) {
        return directionInRadians(bx - ax, by - ay);
    }

    static String toString(double x, double y) {
        StringBuilder str = new StringBuilder();
        toString(x, y, str);
        return str.toString();
    }

    static void toString(double x, double y, Appendable appendable) {
        try {
            appendable.append('[').append(ordToStr(x)).append(',').append(ordToStr(y)).append(']');
        } catch (IOException ex) {
            throw new GeomException("Error writing", ex);
        }
    }
}
