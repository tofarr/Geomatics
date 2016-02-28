package org.jg.geom;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.beans.Transient;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.jg.util.Network;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.VectList;

/**
 *
 * @author tofar_000
 */
public class Line implements Geom, Comparable<Line> {

    final double ax;
    final double ay;
    final double bx;
    final double by;

    Line(Vect a, Vect b) {
        ax = a.x;
        ay = a.y;
        bx = b.x;
        by = b.y;
    }

    Line(double ax, double ay, double bx, double by) {
        this.ax = ax;
        this.ay = ay;
        this.bx = bx;
        this.by = by;
    }

    public static Line valueOf(double ax, double ay, double bx, double by) throws IllegalArgumentException {
        Vect.check(ax, ay);
        Vect.check(bx, by);
        if ((ax == bx) && (ay == by)) {
            throw new IllegalArgumentException("Points were the same! [" + ax + "," + ay + "," + bx + "," + by + "]");
        }
        return new Line(ax, ay, bx, by);
    }

    public double getAx() {
        return ax;
    }

    public double getAy() {
        return ay;
    }

    public double getBx() {
        return bx;
    }

    public double getBy() {
        return by;
    }

    /**
     *
     * @return
     */
    public Vect getA() {
        return new Vect(ax, ay);
    }

    /**
     *
     * @return
     */
    public Vect getB() {
        return new Vect(bx, by);
    }

    /**
     *
     * @param target
     * @return
     */
    public VectBuilder getA(VectBuilder target) {
        target.set(ax, ay);
        return target;
    }

    /**
     *
     * @param target
     * @return
     */
    public VectBuilder getB(VectBuilder target) {
        target.set(bx, by);
        return target;
    }

    /**
     *
     * @return
     */
    public Vect getMid() {
        return new Vect((ax + bx) / 2, (ay + by) / 2);
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
            return new Line(bx, by, ax, ay);
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
    public boolean project(double u, Tolerance tolerance, VectBuilder target) throws NullPointerException, IllegalArgumentException {
        Vect.check(u, "Invalid u {0}");
        return project(ax, ay, bx, by, u, tolerance, target);
    }
    
    static boolean project(double ax, double ay, double bx, double by, double u, Tolerance tolerance, VectBuilder target){
        double x = (u * (bx - ax)) + ax;
        double y = (u * (by - ay)) + ay;
        if (tolerance.match(x, y, ax, ay)) {
            target.set(ax, ay);
            return true;
        } else if (tolerance.match(x, y, bx, by)) {
            target.set(bx, by);
            return true;
        } else {
            target.set(x, y);
            return ((u > 0) && (u < 1));
        }
    }

    /**
     * Project the value given along the line, where 0 represents A and 1
     * represents B. Tolerance is used to snap points to A or B if close enough.
     * Return true if such snapping occurs, or u > 0 or u < 1. Returned point
     * is projected outward a dist distFromLine from the line, where positive 
     * values are on the right, negative values are on the left
     *
     * @param u
     * @param distFromLine the distance from the line (positive on right, negative on left)
     * @param tolerance tolerance for snapping to end points
     * @param target target vector
     * @return true if on segment, false otherwise
     * @throws IllegalArgumentException if u was infinite or NaN
     * @throws NullPointerException if tolerance or target was null
     */
    public boolean projectOutward(double u, double distFromLine, Tolerance tolerance, VectBuilder target) throws NullPointerException, IllegalArgumentException {
        Vect.check(u, "Invalid u {0}");
        Vect.check(distFromLine, "Invalid distance from line {0}");
        return projectOutward(ax, ay, bx, by, u, distFromLine, tolerance, target);
    }
    
    static boolean projectOutward(double ax, double ay, double bx, double by, double u, double distFromLine, Tolerance tolerance, VectBuilder target){
        boolean ret = project(ax, ay, bx, by, u, tolerance, target);
        double dx = bx - ax;
        double dy = by - ay;
        double len = Math.sqrt(dx * dx + dy * dy);
        double mul = Math.abs(distFromLine) / len;
        dx *= mul;
        dy *= mul;
        if(distFromLine < 0){
            target.add(-dy, dx);
        }else{
            target.add(dy, -dx);
        }
        return ret;
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
    public void projectClosest(double u, Tolerance tolerance, VectBuilder target) throws NullPointerException, IllegalArgumentException {
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
        return vectLineDistSq(ax, ay, bx, by, vect.getX(), vect.getY());
    }

    static double vectLineDistSq(double ax, double ay, double bx, double by, double x, double y) {
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
    public boolean intersectionLine(Line line, Tolerance tolerance, VectBuilder target) throws NullPointerException {
        return intersectionLineInternal(ax, ay, bx, by, line.ax, line.ay, line.bx, line.by, tolerance, target);
    }

    static boolean intersectionLineInternal(double ax, double ay, double bx, double by, double jax, double jay, double jbx, double jby, Tolerance tolerance, VectBuilder target) throws NullPointerException {
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
    public boolean intersectionSeg(Line line, Tolerance tolerance, VectBuilder target) throws NullPointerException, IllegalArgumentException {
        return intersectionSegInternal(ax, ay, bx, by, line.ax, line.ay, line.bx, line.by, tolerance, target);
    }

    static boolean intersectionSegInternal(double ax, double ay, double bx, double by, double jax, double jay, double jbx, double jby, Tolerance tolerance, VectBuilder target) throws NullPointerException {
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
    public Rect getBounds() {
        return new RectBuilder().addInternal(ax, ay).addInternal(bx, by).build();
    }

    @Override
    public void addBoundsTo(RectBuilder target) throws NullPointerException {
        target.addInternal(ax, ay).addInternal(bx, by);
    }

    @Override
    public Line transform(Transform transform) throws NullPointerException {
        Vect ta = transform.transform(getA());
        Vect tb = transform.transform(getB());
        return new Line(ta, tb);
    }

    @Override
    public PathIterator pathIterator() {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(ax, ay);
        path.lineTo(bx, by);
        return path.getPathIterator(null);
    }

    @Override
    public void addTo(Network network, Tolerance tolerance) throws NullPointerException, IllegalArgumentException {
        network.addLink(ax, ay, bx, by);
    }

    @Override
    public Geom buffer(double amt, Tolerance tolerance) throws IllegalArgumentException, NullPointerException {
        if(amt == 0){
            return this;
        }else if(amt < 0){
            return null;
        }
        VectList result = new VectList();
        VectBuilder vect = new VectBuilder();
        
        projectOutward(0, -amt, tolerance, vect);
        double ix = vect.getX();
        double iy = vect.getY();
        projectOutward(0, amt, tolerance, vect);
        Vect.linearizeArc(ax, ay, ix, iy, vect.getX(), vect.getY(), Math.abs(amt), tolerance.getTolerance(), result);
        
        projectOutward(1, amt, tolerance, vect);
        double jx = vect.getX();
        double jy = vect.getY();
        projectOutward(1, -amt, tolerance, vect);
        Vect.linearizeArc(bx, by, jx, jy, vect.getX(), vect.getY(), Math.abs(amt), tolerance.getTolerance(), result);
        
        result.add(ix, iy);
        
        return new RingSet(new Ring(result));
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
        hash = 97 * hash + Vect.hash(this.ax);
        hash = 97 * hash + Vect.hash(this.ay);
        hash = 97 * hash + Vect.hash(this.bx);
        hash = 97 * hash + Vect.hash(this.by);
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
        StringBuilder str = new StringBuilder();
        toString(str);
        return str.toString();
    }

    /**
     * Convert this line to a string in the format [ax,ay,bx,by] and add it to
     * the appendable given
     *
     * @param appendable
     * @throws GeomException if there was an output error
     * @throws NullPointerException if appendable was null
     */
    public void toString(Appendable appendable) throws GeomException, NullPointerException {
        try {
            appendable.append('[')
                    .append(Vect.ordToStr(ax)).append(',')
                    .append(Vect.ordToStr(ay)).append(',')
                    .append(Vect.ordToStr(bx)).append(',')
                    .append(Vect.ordToStr(by)).append(']');
        } catch (IOException ex) {
            throw new GeomException("Error writing", ex);
        }
    }

    public void write(DataOutput out) throws IOException {
        out.writeDouble(ax);
        out.writeDouble(ay);
        out.writeDouble(bx);
        out.writeDouble(by);
    }

    public static Line read(DataInput in) throws IOException {
        return valueOf(in.readDouble(), in.readDouble(), in.readDouble(), in.readDouble());
    }

    
    @Override
    public Line clone() {
        return this;
    }

}
