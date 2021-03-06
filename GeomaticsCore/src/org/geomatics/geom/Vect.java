package org.geomatics.geom;

import java.beans.Transient;
import java.io.IOException;
import java.text.MessageFormat;
import org.geomatics.geom.io.GeomIOException;
import org.jayson.JaysonWriter;
import org.geomatics.geom.io.VectHandler;
import org.geomatics.util.Tolerance;
import org.geomatics.util.Transform;
import org.geomatics.util.VectList;

/**
 * Immutable 2D vector / Point. Checks are in place to insure that ordinates are
 * never Infinite or NaN
 *
 * @author tofar_000
 */
public final class Vect implements Geom, Comparable<Vect> {

    public static final String CODE = "PT";
    /**
     * Vector [0,0]
     */
    public static final Vect ZERO = new Vect(0, 0);
    /**
     * x ordinate
     */
    public final double x;
    /**
     * y ordinate
     */
    public final double y;

    Vect(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Get / create a vector with the values given
     *
     * @param x
     * @param y
     * @return a vector
     * @throws IllegalArgumentException if x or y was infinite or NaN
     */
    public static Vect valueOf(double x, double y) throws IllegalArgumentException {
        check(x, y);
        if ((x == 0) && (y == 0)) {
            return ZERO;
        }
        return new Vect(x, y);
    }

    /**
     * Get the x value
     *
     * @return x;
     */
    public double getX() {
        return x;
    }

    /**
     * Get the y value
     *
     * @return y;
     */
    public double getY() {
        return y;
    }

    /**
     * Create a builder based on this vector
     *
     * @return
     */
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
     * @throws NullPointerException if vect was null
     */
    public double dist(Vect vect) throws NullPointerException {
        return Math.sqrt(distSq(vect));
    }

    /**
     * Get the square of the distance between this vector and the vector given
     *
     * @param vect
     * @return
     * @throws NullPointerException if vect was null
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
    @Transient
    public Rect getBounds() {
        return new Rect(x, y, x, y);
    }

    @Override
    public Geom transform(Transform transform) throws NullPointerException {
        return transform.transform(this);
    }

    @Override
    public PathIter iterator() {
        return new PathIter() {
            int state;

            @Override
            public boolean isDone() {
                return (state >= 2);
            }

            @Override
            public void next() {
                state++;
            }

            @Override
            public PathSegType currentSegment(double[] coords) throws IllegalStateException {
                if (state < 2) {
                    coords[0] = x;
                    coords[1] = y;
                    return (state == 0) ? PathSegType.MOVE : PathSegType.LINE;
                }
                throw new IllegalStateException();
            }
        };
    }

    @Override
    public GeoShape toGeoShape(Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        GeoShape ret = new GeoShape(null, null, new PointSet(new VectList().add(this)));
        return ret;
    }

    @Override
    public void addTo(Network network, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        network.addVertex(this);
    }

    @Override
    public int relate(Vect vect, Tolerance accuracy) throws NullPointerException {
        return match(vect, accuracy) ? Relation.TOUCH : Relation.DISJOINT;
    }

    @Override
    public int relate(VectBuilder vect, Tolerance accuracy) throws NullPointerException {
        return accuracy.match(x, y, vect.getX(), vect.getY()) ? Relation.TOUCH : Relation.DISJOINT;
    }

    @Override
    public int relate(Geom geom, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        if (geom instanceof Vect) {
            return relate((Vect) geom, accuracy);
        }
        int ret = geom.relate(this, accuracy);
        ret = Relation.swap(ret);
        return ret;
    }

    @Override
    public Geom buffer(double amt, Linearizer linearizer, Tolerance tolerance) throws IllegalArgumentException, NullPointerException {
        check(amt, "Invalid buffer amt {0}");
        if (amt == 0) {
            return this;
        } else if (amt < 0) {
            return null;
        } else {
            VectList result = new VectList();
            double angleSize = 2 * Math.PI;
            double sx = x + amt;
            result.add(sx, y);
            linearizer.linearizeSegment(x, y, sx, y, angleSize, result);
            if (result.size() < 4) {
                return this;
            }
            Ring ring = new Ring(result, null, null, null, this, true);
            return ring;
        }
    }

    /**
     * Get a PointSet version of this Vector
     *
     * @return
     */
    public PointSet toPointSet() {
        VectList vects = new VectList(1);
        vects.add(this);
        return new PointSet(vects);
    }

    @Override
    public Geom union(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        if (other instanceof Vect) {
            return union((Vect) other, accuracy);
        }
        if (other.relate(this, accuracy) != Relation.DISJOINT) {
            return other;
        }
        return other.union(this, linearizer, accuracy);
    }

    /**
     * Get the union of this vect and the other vect given
     *
     * @param other
     * @param accuracy
     * @return
     */
    public Geom union(Vect other, Tolerance accuracy) {
        if (accuracy.match(x, y, other.x, other.y)) {
            return this;
        }
        VectList points = new VectList(2);
        points.add((Vect) other);
        points.add(this);
        points.sort();
        return new PointSet(points);
    }

    @Override
    public Vect intersection(Geom other, Linearizer linearizer, Tolerance tolerance) throws NullPointerException {
        if (other.relate(this, tolerance) == Relation.DISJOINT) {
            return null;
        }
        return this;
    }

    @Override
    public Vect less(Geom other, Linearizer linearizer, Tolerance tolerance) throws NullPointerException {
        if (other.relate(this, tolerance) == Relation.DISJOINT) {
            return this;
        }
        return null;
    }

    @Override
    public Geom xor(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException, IllegalArgumentException {
        if (other instanceof Vect) {
            return xor((Vect) other, accuracy);
        } else if (other.relate(this, accuracy) != Relation.DISJOINT) {
            return other;
        }
        return other.xor(this, linearizer, accuracy);
    }

    /**
     * xor this vect and that given
     *
     * @param other
     * @param accuracy
     * @return null if points same, pointset if different
     */
    public Geom xor(Vect other, Tolerance accuracy) {
        if (this.match(other, accuracy)) {
            return null;
        } else {
            VectList vects = new VectList();
            vects.add(this);
            vects.add(other);
            vects.sort();
            return new PointSet(vects);
        }
    }

    @Override
    public double getArea(Linearizer linearizer, Tolerance accuracy) {
        return 0;
    }

    /**
     * Determine if this vect matches the vector given within the tolerance
     * given
     *
     * @param vect
     * @param tolerance
     * @return
     * @throws NullPointerException if vect or tolerance was null
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
        StringBuilder str = new StringBuilder();
        new VectHandler().render(this, new JaysonWriter(str));
        return str.toString();
    }

    public String toWkt() {
        StringBuilder str = new StringBuilder();
        toWkt(str);
        return str.toString();
    }

    public void toWkt(Appendable appendable) throws GeomIOException {
        try {
            appendable.append("POINT(").append(ordToStr(x)).append(' ').append(ordToStr(y)).append(')');
        } catch (IOException ex) {
            throw new GeomIOException("Error writing", ex);
        }
    }

    /**
     * Check the vector given
     *
     * @param x
     * @param y
     * @throws IllegalArgumentException if x or y was infinite or NaN
     */
    public static void check(double x, double y) throws IllegalArgumentException {
        check(x, "Invalid x : {0}");
        check(y, "Invalid y : {0}");
    }

    /**
     * Check the ordinate given
     *
     * @param ord
     * @param pattern pattern for error message of there was an error
     * @throws IllegalArgumentException if x or y was infinite or NaN
     * @throws NullPointerException if pattern was null
     */
    public static void check(double ord, String pattern) throws IllegalArgumentException, NullPointerException {
        if (Double.isInfinite(ord) || Double.isNaN(ord)) {
            throw new IllegalArgumentException(MessageFormat.format(pattern, Double.toString(ord)));
        }
    }

    /**
     * Convert the ordinate given to a string. If there is no decimal component
     * (string ends with .0) style as integer
     *
     * @param ord
     * @return
     */
    public static String ordToStr(double ord) {
        String ret = Double.toString(ord);
        if (ret.endsWith(".0")) {
            ret = ret.substring(0, ret.length() - 2);
        }
        return ret;
    }

    /**
     * Get a hash for the value given, suitable for use in open hash maps
     *
     * @param value
     * @parma value
     * @return
     */
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

    /**
     * Get a hash for the vector given, suitable for use in open hash maps
     *
     * @param x
     * @param y
     * @return
     */
    public static int hashCode(double x, double y) {
        int hash = 5;
        hash = 47 * hash + hash(x);
        hash = 47 * hash + hash(y);
        return hash;
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
    public static double dot(double ax, double ay, double bx, double by) {
        double ret = (ax * bx) + (ay * by);
        return ret;
    }

    /**
     * Get the square of the distance between the vectors given
     *
     * @param ax
     * @param ay
     * @param bx
     * @param by
     * @return
     */
    public static double distSq(double ax, double ay, double bx, double by) {
        double dx = bx - ax;
        double dy = by - ay;
        double ret = (dx * dx) + (dy * dy);
        return ret;
    }

    /**
     * Get the dydx of the line between the vectors given
     *
     * @param ax
     * @param ay
     * @param bx
     * @param by
     * @return
     */
    public static double dydxTo(double ax, double ay, double bx, double by) {
        double dx = bx - ax;
        double dy = by - ay;
        double ret = dy / dx;
        return ret;
    }

    /**
     * Get the direction in radians of the vector given
     *
     * @param x
     * @param y
     * @return
     * @throws IllegalArgumentException if the vector is 0,0
     */
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

    /**
     * Get the direction of the vector from a to b
     *
     * @param ax
     * @param ay
     * @param bx
     * @param by
     * @return
     * @throws IllegalArgumentException if the vectors were the same
     */
    public static double directionInRadiansTo(double ax, double ay, double bx, double by) throws IllegalArgumentException {
        return directionInRadians(bx - ax, by - ay);
    }
}
