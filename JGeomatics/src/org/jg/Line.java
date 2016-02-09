package org.jg;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.beans.ConstructorProperties;
import java.beans.Transient;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Class representing a 2D line. A line is considered invalid if the two end
 * points are the same
 *
 * @author tim.ofarrell
 */
public final class Line implements PathIterable, Externalizable, Cloneable, Comparable<Line> {

    double ax;
    double ay;
    double bx;
    double by;

    /**
     * Create a new instance of Line.
     */
    public Line() {
    }

    /**
     * Create a new instance of Line
     *
     * @param ax
     * @param ay
     * @param bx
     * @param by
     * @throws IllegalArgumentException if an ordinate was infinite or NaN
     */
    @ConstructorProperties({"ax", "ay", "bx", "by"})
    public Line(double ax, double ay, double bx, double by) throws IllegalArgumentException {
        set(ax, ay, bx, by);
    }

    /**
     * Set the line to that given
     *
     * @param a
     * @param b
     * @throws NullPointerException if a or b was null
     */
    public Line(Vect a, Vect b) throws NullPointerException {
        set(a, b);
    }

    /**
     * Set the line to that given
     *
     * @param ax
     * @param ay
     * @param bx
     * @param by
     * @return
     * @throws IllegalArgumentException if any ordinate was Infinite or NaN
     */
    public Line set(double ax, double ay, double bx, double by) throws IllegalArgumentException {
        Util.check(ax, "Invalid ax : {0}");
        Util.check(ay, "Invalid ay : {0}");
        Util.check(bx, "Invalid bx : {0}");
        Util.check(by, "Invalid by : {0}");
        this.ax = ax;
        this.ay = ay;
        this.bx = bx;
        this.by = by;
        return this;
    }

    /**
     *
     * @param a
     * @param b
     * @return
     * @throws NullPointerException
     */
    public Line set(Vect a, Vect b) throws NullPointerException {
        this.ax = a.getX();
        this.ay = a.getY();
        this.bx = b.getX();
        this.by = b.getY();
        return this;
    }

    /**
     *
     * @return
     */
    public double getAx() {
        return ax;
    }

    /**
     *
     * @param ax
     * @throws IllegalArgumentException
     */
    public void setAx(double ax) throws IllegalArgumentException {
        Util.check(ax, "Invalid ax : {0}");
        this.ax = ax;
    }

    /**
     *
     * @return
     */
    public double getAy() {
        return ay;
    }

    /**
     *
     * @param ay
     * @throws IllegalArgumentException
     */
    public void setAy(double ay) throws IllegalArgumentException {
        Util.check(ay, "Invalid ay : {0}");
        this.ay = ay;
    }

    /**
     *
     * @return
     */
    public double getBx() {
        return bx;
    }

    /**
     *
     * @param bx
     * @throws IllegalArgumentException
     */
    public void setBx(double bx) throws IllegalArgumentException {
        Util.check(bx, "Invalid bx : {0}");
        this.bx = bx;
    }

    /**
     *
     * @return
     */
    public double getBy() {
        return by;
    }

    /**
     *
     * @param by
     * @throws IllegalArgumentException
     */
    public void setBy(double by) throws IllegalArgumentException {
        Util.check(by, "Invalid by : {0}");
        this.by = by;
    }

    /**
     *
     * @param target
     * @return
     */
    public Vect getA(Vect target) {
        target.set(ax, ay);
        return target;
    }

    /**
     *
     * @param target
     * @return
     */
    public Vect getB(Vect target) {
        target.set(bx, by);
        return target;
    }

    /**
     *
     * @param target
     * @return
     */
    public Vect getMid(Vect target) {
        target.set((ax + bx) / 2, (ay + by) / 2);
        return target;
    }

    /**
     *
     * @param target
     * @return
     */
    @Override
    public Rect getBounds(Rect target) {
        return target.reset().unionInternal(ax, ay).unionInternal(bx, by);
    }

    /**
     *
     * @param tolerance
     * @return
     */
    public boolean isValid(Tolerance tolerance) {
        return (!tolerance.match(ax, ay, bx, by));
    }

    /**
     *
     * @return
     */
    public Line normalize() {
        if (Vect.compare(ax, ay, bx, by) > 0) {
            double t;
            t = ax;
            ax = bx;
            bx = t;
            t = ay;
            ay = by;
            by = t;
        }
        return this;
    }

    /**
     * Get the length of the line Note: May fail if line is invalid
     *
     * @return
     */
    @Transient
    public double getLength() {
        return Math.sqrt(getLengthSq());
    }

    /**
     * Get the square of the length of the line (faster than getting the length,
     * and sufficient for some operations)
     *
     * @return
     */
    @Transient
    public double getLengthSq() {
        return Vect.distSq(ax, ay, bx, by);
    }

    /**
     * Get the rate of change of y with respect to x
     *
     * @return
     */
    @Transient
    public double getDydx() {
        return Vect.dydxTo(ax, ay, bx, by);
    }

    /**
     * Get the direction from a to b in radians
     *
     * @return
     */
    @Transient
    public double getDirectionInRadians() {
        return Vect.directionInRadiansTo(ax, ay, bx, by);
    }

    /**
     * Get the side of the line on which vect lies. Positive values imply that
     * it lies on the left of the line. Negative values imply it lies on the
     * right of the line. 0 implies it lies on the line
     *
     * @param vect
     * @param tolerance
     * @return
     * @throws IllegalStateException if invalid
     */
    public int sign(Vect vect, Tolerance tolerance) throws NullPointerException {
        return tolerance.check(sign(vect));
    }

    /**
     * Get the side of the line on which vect lies. Positive values imply that
     * it lies on the left of the line. Negative values imply it lies on the
     * right of the line. 0 implies it lies on the line
     *
     * @param vect
     * @return
     * @throws IllegalStateException if invalid
     */
    public double sign(Vect vect) throws NullPointerException {
        return sign(ax, ay, bx, by, vect.getX(), vect.getY());
    }

    static double sign(double ax, double ay, double bx, double by, double x, double y) {
        double ret = ((bx - ax) * (y - ay)) - ((by - ay) * (x - ax));
        return ret;
    }

    /**
     * Project the value given along the line, where 0 represents A and 1
     * represents B. Tolerance is used to snap points to A or B if close enough.
     * Return true if such snapping occurs, or u > 0 or u < 1.
     *
     * @param u
     * @param tolerance tolerance for snapping to end points
     * @param target target vector
     * @return true if on segment, false otherwise
     * @throws IllegalArgumentException if u was infinite or NaN
     * @throws NullPointerException if tolerance or target was null
     */
    public boolean project(double u, Tolerance tolerance, Vect target) throws NullPointerException, IllegalArgumentException {
        double x = (u * (bx - ax)) + ax;
        double y = (u * (by - ay)) + ay;
        if (tolerance.match(x, y, ax, ay)) {
            getA(target);
            return true;
        } else if (tolerance.match(x, y, bx, by)) {
            getB(target);
            return true;
        } else {
            target.set(x, y);
            return ((u > 0) && (u < 1));
        }
    }

    /**
     * Project the value given along the line segment, where 0 represents A and
     * 1 represents B. Tolerance is used to snap points to A or B if close
     * enough. If u < 0 return A and if u > 1 return B and the point is beyond
     * the tolerance, return null
     *
     * @param u
     * @param tolerance tolerance for snapping to end points
     * @param target target vector
     * @throws IllegalArgumentException if u was infinite or NaN
     * @throws NullPointerException if tolerance or target was null
     */
    public void projectClosest(double u, Tolerance tolerance, Vect target) throws NullPointerException, IllegalArgumentException {
        double x = (u * (bx - ax)) + ax;
        double y = (u * (by - ay)) + ay;
        if ((u < 0) || tolerance.match(x, y, ax, ay)) {
            getA(target);
        } else if ((u > 1) || tolerance.match(x, y, bx, by)) {
            getB(target);
        } else {
            target.set(x, y);
        }
    }

    /**
     * Get the min distance from this line to the point given
     *
     * @param vect
     * @return
     * @throws NullPointerException if vect was null
     */
    public double distLineVect(Vect vect) throws NullPointerException {
        return Math.sqrt(distLineVectSq(vect));
    }

    /**
     * Get the square of the min distance from this line to the point given
     *
     * @param vect
     * @return
     * @throws NullPointerException if vect was null
     */
    public double distLineVectSq(Vect vect) throws NullPointerException {
        return pntLineDistSq(ax, ay, bx, by, vect.getX(), vect.getY());
    }

    static double pntLineDistSq(double ax, double ay, double bx, double by, double x, double y) {
        bx -= ax;
        by -= ay;
        x -= ax;
        y -= ay;
        double dot = Vect.dot(x, y, bx, by);
        double projlenSq = dot * dot / (bx * bx + by * by);
        double lenSq = Math.max(x * x + y * y - projlenSq, 0);
        return lenSq;
    }

    /**
     * Get the min distance from this line to the point given
     *
     * @param vect
     * @return
     * @throws NullPointerException if vect was null
     */
    public double distSegVect(Vect vect) throws NullPointerException {
        return Math.sqrt(distSegVectSq(vect));
    }

    /**
     * Get the min distance from this line to the point given
     *
     * @param vect
     * @return
     * @throws NullPointerException if vect was null
     */
    public double distSegVectSq(Vect vect) throws NullPointerException {
        return pntSegDistSq(ax, ay, bx, by, vect.getX(), vect.getY());
    }

    static double pntSegDistSq(double ax, double ay, double bx, double by, double x, double y) {
        bx -= ax;
        by -= ay;
        x -= ax;
        y -= ay;
        double dot = Vect.dot(x, y, bx, by);
        double projlenSq;
        if (dot <= 0.0) {
            projlenSq = 0.0;
        } else {
            x = bx - x;
            y = by - y;
            dot = x * bx + y * by;
            if (dot <= 0.0) {
                projlenSq = 0.0;
            } else {
                projlenSq = dot * dot / (bx * bx + by * by);
            }
        }
        double lenSq = Math.max(x * x + y * y - projlenSq, 0);
        return lenSq;
    }

    /**
     * Determine if this line is parallell to that given, with differences in
     * slope within the tolerance
     *
     * @param line
     * @param tolerance
     * @return
     * @throws NullPointerException if line or tolerance was null
     */
    public boolean isParallel(Line line, Tolerance tolerance) throws NullPointerException {
        double denom = getDenom(ax, ay, bx, by, line.ax, line.ay, line.bx, line.by);
        return tolerance.match(denom, 0);
    }

    static double getDenom(double iax, double iay, double ibx, double iby,
            double jax, double jay, double jbx, double jby) {
        return (jby - jay) * (ibx - iax) - (jbx - jax) * (iby - iay);
    }

    /**
     * Determine if segments intersect. Segments which do not intersect, but are
     * within the tolerance given from each other are considered to intersect
     * unless they are parallel
     *
     * @param line
     * @param tolerance
     * @return true if segments intersect, false otherwise
     * @throws NullPointerException if line or tolerance was null
     */
    public boolean intersectsSeg(Line line, Tolerance tolerance) throws NullPointerException {
        double jax = line.ax;
        double jay = line.ay;
        double jbx = line.bx;
        double jby = line.by;
        double denom = getDenom(ax, ay, bx, by, jax, jay, jbx, jby);
        if (denom == 0.0) { // Lines are parallel.
            return false;
        }
        double ui = ((jbx - jax) * (ay - jay) - (jby - jay) * (ax - jax)) / denom; // projected distance along i and j
        double uj = ((bx - ax) * (ay - jay) - (by - ay) * (ax - jax)) / denom;

        if ((ui >= 0) && (ui <= 1) && (uj >= 0) && (uj <= 1)) {
            return true;
        }
        double x = (ui * (bx - ax)) + ax;
        double y = (ui * (by - ay)) + ay;

        if (ui < 0) {
            if (!tolerance.match(x, y, ax, ay)) {
                return false;
            }
        } else if (ui > 1) {
            if (!tolerance.match(x, y, bx, by)) {
                return false;
            }
        }
        if (uj < 0) {
            if (!tolerance.match(x, y, jax, jay)) {
                return false;
            }
        } else if (uj > 1) {
            if (!tolerance.match(x, y, jbx, jby)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the intersection of this line and that given. Lines which do not
     * intersect, but are within the tolerance given from each other are
     * considered to intersect at their end points unless they are parallel
     *
     * @param line
     * @param tolerance
     * @param target target vector
     * @return true if segments intersect, false otherwise
     * @throws NullPointerException if line or tolerance was null
     * @throws IllegalStateException if invalid
     */
    public boolean intersectionLine(Line line, Tolerance tolerance, Vect target) throws NullPointerException {
        return intersectionLineInternal(ax, ay, bx, by, line.ax, line.ay, line.bx, line.by, tolerance, target);
    }

    static boolean intersectionLineInternal(double ax, double ay, double bx, double by, double jax, double jay, double jbx, double jby, Tolerance tolerance, Vect target) throws NullPointerException {
        if (Vect.compare(ax, ay, bx, by) > 0) {
            double tmp = ax;
            ax = bx;
            bx = tmp;
            tmp = ay;
            ay = by;
            by = tmp;
        }
        if (Vect.compare(jax, jay, jbx, jby) > 0) {
            double tmp = jax;
            jax = jbx;
            jbx = tmp;
            tmp = jay;
            jay = jby;
            jby = tmp;
        }
        if (compare(ax, ay, bx, by, jax, jay, jbx, jby) > 0) {
            double tmp = ax;
            ax = jax;
            jax = tmp;

            tmp = ay;
            ay = jay;
            jay = tmp;

            tmp = bx;
            bx = jbx;
            jbx = tmp;

            tmp = by;
            by = jby;
            jby = tmp;
        }

        double denom = getDenom(ax, ay, bx, by, jax, jay, jbx, jby);
        if (denom == 0.0) { // Lines are parallel.
            return false;
        }
        double ui = ((jbx - jax) * (ay - jay) - (jby - jay) * (ax - jax)) / denom; // projected distance along i and j

        double x = (ui * (bx - ax)) + ax;
        double y = (ui * (by - ay)) + ay;

        if (tolerance.match(x, y, ax, ay)) {
            target.set(ax, ay);
        } else if (tolerance.match(x, y, bx, by)) {
            target.set(bx, by);
        } else if (tolerance.match(x, y, jax, jay)) {
            target.set(jax, jay);
        } else if (tolerance.match(x, y, jbx, jby)) {
            target.set(jbx, jby);
        } else {
            target.set(x, y);
        }
        return true;
    }

    /**
     * Get the intersection of this segment and that given. Segments which do
     * not intersect, but are within the tolerance given from each other are
     * considered to intersect at their end points unless they are parallel
     *
     * @param line
     * @param tolerance
     * @param target target vector
     * @return true if segments intersect, false otherwise
     * @throws NullPointerException if tolerance was null
     * @throws IllegalArgumentException if an ordinate was NaN or infinite
     */
    public boolean intersectionSeg(Line line, Tolerance tolerance, Vect target) throws NullPointerException, IllegalArgumentException {
        return intersectionSegInternal(ax, ay, bx, by, line.ax, line.ay, line.bx, line.by, tolerance, target);
    }

    static boolean intersectionSegInternal(double ax, double ay, double bx, double by, double jax, double jay, double jbx, double jby, Tolerance tolerance, Vect target) throws NullPointerException {
        if (Vect.compare(ax, ay, bx, by) > 0) {
            double tmp = ax;
            ax = bx;
            bx = tmp;
            tmp = ay;
            ay = by;
            by = tmp;
        }
        if (Vect.compare(jax, jay, jbx, jby) > 0) {
            double tmp = jax;
            jax = jbx;
            jbx = tmp;
            tmp = jay;
            jay = jby;
            jby = tmp;
        }
        if (compare(ax, ay, bx, by, jax, jay, jbx, jby) > 0) {
            double tmp = ax;
            ax = jax;
            jax = tmp;

            tmp = ay;
            ay = jay;
            jay = tmp;

            tmp = bx;
            bx = jbx;
            jbx = tmp;

            tmp = by;
            by = jby;
            jby = tmp;
        }

        double denom = getDenom(ax, ay, bx, by, jax, jay, jbx, jby);
        if (denom == 0.0) { // Lines are parallel.
            return false;
        }
        double ui = ((jbx - jax) * (ay - jay) - (jby - jay) * (ax - jax)) / denom; // projected distance along i and j
        double uj = ((bx - ax) * (ay - jay) - (by - ay) * (ax - jax)) / denom;

        double x = (ui * (bx - ax)) + ax;
        double y = (ui * (by - ay)) + ay;

        boolean ia = tolerance.match(x, y, ax, ay);
        boolean ib = tolerance.match(x, y, bx, by);
        boolean i = ia || ib || ((ui >= 0) && (ui <= 1));

        boolean ja = tolerance.match(x, y, jax, jay);
        boolean jb = tolerance.match(x, y, jbx, jby);
        boolean j = ja || jb || ((uj >= 0) && (uj <= 1));

        if (i && j) {
            if (ia) {
                target.set(ax, ay);
            } else if (ib) {
                target.set(bx, by);
            } else if (ja) {
                target.set(jax, jay);
            } else if (jb) {
                target.set(jbx, jby);
            } else {
                target.set(x, y);
            }
            return true;
        }
        return false;
    }

    @Override
    public PathIterator getPathIterator() {
        return getPathIterator(null);
    }

    @Override
    public PathIterator getPathIterator(final AffineTransform transform) {
        return new PathIterator(){

            int index = 0;
                
            @Override
            public int getWindingRule() {
                return WIND_EVEN_ODD;
            }

            @Override
            public boolean isDone() {
                return index > 1;
            }

            @Override
            public void next() {
                index++;
            }

            @Override
            public int currentSegment(float[] coords) {
                switch(index){
                    case 0:
                        coords[0] = (float)ax;
                        coords[1] = (float)ay;
                        if(transform != null){
                            transform.transform(coords, 0, coords, 0, 1);
                        }
                        return SEG_MOVETO;
                    case 1:
                        coords[0] = (float)bx;
                        coords[1] = (float)by;
                        if(transform != null){
                            transform.transform(coords, 0, coords, 0, 1);
                        }
                        return SEG_LINETO;
                    default:
                        throw new IllegalStateException();
                }
            }

            @Override
            public int currentSegment(double[] coords) {
                switch(index){
                    case 0:
                        coords[0] = ax;
                        coords[1] = ay;
                        if(transform != null){
                            transform.transform(coords, 0, coords, 0, 1);
                        }
                        return SEG_MOVETO;
                    case 1:
                        coords[0] = bx;
                        coords[1] = by;
                        if(transform != null){
                            transform.transform(coords, 0, coords, 0, 1);
                        }
                        return SEG_LINETO;
                    default:
                        throw new IllegalStateException();
                }
            }
                
        };
    }

    @Override
    public PathIterator getPathIterator(AffineTransform transform, double flatness) throws NullPointerException {
        return getPathIterator(transform);
    }
    
    
    @Override
    public int compareTo(Line other) {
        return compare(ax, ay, bx, by, other.ax, other.ay, other.bx, other.by);
    }

    static int compare(double iax, double iay, double ibx, double iby,
            double jax, double jay, double jbx, double jby) {
        int ret = Vect.compare(iax, iay, jax, jay);
        if (ret == 0) {
            ret = Vect.compare(ibx, iby, jbx, jby);
        }
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Util.hash(this.ax);
        hash = 97 * hash + Util.hash(this.ay);
        hash = 97 * hash + Util.hash(this.bx);
        hash = 97 * hash + Util.hash(this.by);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Line) {
            Line line = (Line) obj;
            return (ax == line.ax) && (ay == line.ay) && (bx == line.bx) && (by == line.by);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return '[' + Util.ordToStr(ax) + ',' + Util.ordToStr(ay) + ',' + Util.ordToStr(bx) + ',' + Util.ordToStr(by) + ']';
    }

    /**
     * Convert this line to a string in the format [ax,ay,bx,by] and add it to
     * the appendable given
     *
     * @param appendable
     * @throws IOException if there was an output error
     * @throws NullPointerException if appendable was null
     */
    public void toString(Appendable appendable) throws IOException, NullPointerException {
        appendable.append('[')
                .append(Util.ordToStr(ax)).append(',')
                .append(Util.ordToStr(ay)).append(',')
                .append(Util.ordToStr(bx)).append(',')
                .append(Util.ordToStr(by)).append(']');
    }

    @Override
    public Line clone() {
        return new Line(ax, ay, bx, by);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        writeData(out);
    }

    /**
     * Write this line to the output given
     *
     * @param out
     * @throws IOException if out was null
     * @throws NullPointerException if out was null
     */
    public void writeData(DataOutput out) throws IOException, NullPointerException {
        out.writeDouble(ax);
        out.writeDouble(ay);
        out.writeDouble(bx);
        out.writeDouble(by);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        readData(in);
    }

    /**
     * Set the ordinates for this line from the input given
     * @param in
     * @return this
     * @throws IOException if there was an error
     * @throws NullPointerException if in was null
     */
    public Line readData(DataInput in) throws IOException, NullPointerException {
        return set(in.readDouble(), in.readDouble(), in.readDouble(), in.readDouble());
    }

    /**
     * Read a line from the input given
     * @param in
     * @return a line
     * @throws IOException if there was an error
     * @throws NullPointerException if in was null
     */
    public static Line read(DataInput in) throws IOException, NullPointerException {
        return new Line().readData(in);
    }

}
