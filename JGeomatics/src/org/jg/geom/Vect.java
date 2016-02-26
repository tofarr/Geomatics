package org.jg.geom;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.text.MessageFormat;
import org.jg.util.Network;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.VectList;

/**
 * Immutable vector
 *
 * @author tofar_000
 */
public final class Vect implements Geom, Comparable<Vect> {

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
    public Rect getBounds() {
        return new Rect(x, y, x, y);
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

    @Override
    public Geom buffer(double amt, double flatness, Tolerance tolerance) throws IllegalArgumentException, NullPointerException {
        check(amt, "Invalid buffer amt {0}");
        check(amt, "Invalid flatness {0}");
        if(amt <= 0){
            throw new IllegalArgumentException("Invalid flatness "+flatness);
        }
        if(amt == 0){
            return this;
        }else if(amt < 0){
            return null;
        }else{
            VectList result = new VectList();
            double angleSize = 2 * Math.PI;
            double sy = y - amt;
            double radiusSq = amt * amt;
            double flatnessSq = flatness * flatness;
            linearizeArcSegment(x, y, angleSize, x, sy, x, sy, amt, radiusSq, flatnessSq, result);
            return (result.size() > 2) ? new RingSet(new Ring(result)) : this;
        }
    }
    
    
    //Assumes CCW direction
    static void linearizeArc(double ox, double oy, double ax, double ay, double bx, double by, double radius, double flatness, VectList result) throws NullPointerException{
        double angleA = Vect.directionInRadiansTo(ox, oy, ax, ay);
        double angleB = Vect.directionInRadiansTo(ox, oy, bx, by);
        double angleSize = angleB - angleA;
        if(angleSize < 0){
            angleSize += 2 * Math.PI;
        }
        double radiusSq = radius * radius;
        double flatnessSq = flatness * flatness;
        linearizeArcSegment(ox, oy, angleSize, ax, ay, bx, by, radius, radiusSq, flatnessSq, result);
        result.add(bx, by);
    }
    
    static void linearizeArc(double ox, double oy, double angleA, double angleB, double radius, double flatness, VectList result) throws NullPointerException{
        double angleSize = angleB - angleA;
        if(angleSize < 0){
            angleSize += 2 * Math.PI;
        }
        double ax = ox + (Math.cos(angleA) * radius);
        double ay = oy + (Math.sin(angleA) * radius);
        double bx = ox + (Math.cos(angleB) * radius);
        double by = oy + (Math.sin(angleB) * radius);
        double radiusSq = radius * radius;
        double flatnessSq = flatness * flatness;
        linearizeArcSegment(ox, oy, angleSize, ax, ay, bx, by, radius, radiusSq, flatnessSq, result);
        result.add(bx, by);
    }
    
    private static void linearizeArcSegment(double ox, double oy, double angleSize, double ax, double ay, double bx, double by,
            double radius, double radiusSq, double flatnessSq, VectList result){
        
        double mx = (ax + bx) / 2; //get mid point between a and b
        double my = (ay + by) / 2;
        
        if(angleSize > (2 * Math.PI)){ // If angle is greater than 180 degrees, invert vector direction.
            mx = ox + (ox - mx);
            my = oy + (oy - my);
        }
        
        double distSq = Vect.distSq(ox, oy, mx, my);
        double diffSq = radiusSq - distSq;
        if(diffSq <= flatnessSq){ // If the value is less than flatnes, simply add a - b will be added later
            result.add(ax, ay);
        }else{
            
            double nx = Math.pow(mx - ox, 2) * radius / distSq + ox; //calculate a new mid point
            double ny = Math.pow(my - oy, 2) * radius / distSq + oy;
            angleSize /= 2; // angle size is halved
            
            linearizeArcSegment(ox, oy, angleSize, ax, ay, nx, ny, radius, radiusSq, flatnessSq, result);
            linearizeArcSegment(ox, oy, angleSize, nx, ny, bx, by, radius, radiusSq, flatnessSq, result);
        }
        
    }

//    /**
//     * Get the number of segments in the linearized arc between the segments given
//     * @param x origin x
//     * @param y origin y
//     * @param startAngle in radians
//     * @param endAngle in radians
//     * @param radius radius of arc
//     * @param flatness densification factor
//     * @param result list to store densified arc 
//     * @throws IllegalArgumentException if radius or flatness wass <= 0, or any value was infinite or NaN
//     * @throws NullPointerException if result was null
//     */
//    public static void densify(double x, double y, double startAngle, double endAngle, double radius, double flatness, VectList result) throws IllegalArgumentException, NullPointerException{
//        check(startAngle, "Invalid startAngle {0}");
//        check(endAngle, "Invalid endAngle {0}");
//        check(radius, "Invalid radius {0}");
//        if(radius <= 0){
//            throw new IllegalArgumentException("Invalid radius "+radius);
//        }
//        check(flatness, "Invalid flatness {0}");
//        if(flatness <= 0){
//            throw new IllegalArgumentException("Invalid flatness "+flatness);
//        }
//        int numSegments = getNumSegments(startAngle, endAngle, radius, flatness);
//        double segmentSize = (endAngle - startAngle) / numSegments;
//        for(int i = 0; i <= numSegments; i++){
//            double theta = (segmentSize * i) + startAngle;
//            double sx = x + radius * Math.cos(theta);
//            double sy = y + radius * Math.sin(theta);
//            result.add(sx, sy);
//        }
//    }
//    
//    /**
//     * Given the angles and radius given, get the number of segments required such that the difference between the radius and the 
//     * line between the 2 segments is less than flatness
//     */
//    static int getNumSegments(double startAngle, double endAngle, double radius, double flatness) throws IllegalArgumentException{
//        double theta = Math.asin(Math.sqrt(flatness / radius)) * 2;
//        if(endAngle < startAngle){
//            theta = -theta;
//        }
//        int numSegments = (int)Math.ceil((endAngle - startAngle) / theta);
//        return numSegments;
//    }

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
        return toString(x, y);
    }

    @Override
    public void toString(Appendable appendable) throws NullPointerException, GeomException {
        toString(x, y, appendable);
    }

    /**
     * Write this vect to the DataOutput given
     *
     * @param out
     * @throws NullPointerException if out was null
     * @throws GeomException if there was an IO error
     */
    public void write(DataOutput out) throws NullPointerException, GeomException {
        write(x, y, out);
    }

    /**
     * Write the vector given to the DataOutput given
     *
     * @param out
     * @throws NullPointerException if out was null
     * @throws GeomException if there was an IO error
     */
    public static void write(double x, double y, DataOutput out) throws NullPointerException, GeomException {
        try {
            out.writeDouble(x);
            out.writeDouble(y);
        } catch (IOException ex) {
            throw new GeomException("Error writing vector", ex);
        }
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
    public static Vect read(DataInput in) throws NullPointerException, IllegalArgumentException, 
        GeomException {
        try {
            return Vect.valueOf(in.readDouble(), in.readDouble());
        } catch (IOException ex) {
            throw new GeomException("Error reading vector", ex);
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
