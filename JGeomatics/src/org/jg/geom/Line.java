package org.jg.geom;

import java.beans.Transient;
import java.io.IOException;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.VectList;

/**
 * Immutable 2D Line Segment defined by two end points. Checks are in place to insure that ordinates
 * are never infinite or NaN, and that the two end points are not the same
 *
 * @author tofar_000
 */
public final class Line implements Geom, Comparable<Line> {

    public static final String CODE = "LN";

    /**
     * ax
     */
    public final double ax;
    /**
     * ay
     */
    public final double ay;
    /**
     * bx
     */
    public final double bx;
    /**
     * by
     */
    public final double by;

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

    /**
     * Get / Create a line segment based on the 2 end points given
     *
     * @param ax
     * @param ay
     * @param bx
     * @param by
     * @return a line segment
     * @throws IllegalArgumentException if an ordinate was Infinite or NaN, or A and B were the same
     * point
     */
    public static Line valueOf(double ax, double ay, double bx, double by) throws IllegalArgumentException {
        check(ax, ay, bx, by);
        return new Line(ax, ay, bx, by);
    }

    /**
     * Check that the line given is valid
     *
     * @param ax
     * @param ay
     * @param bx
     * @param by
     * @throws IllegalArgumentException if ordinate was infinite or NaN or ordinates were same
     */
    public static void check(double ax, double ay, double bx, double by) throws IllegalArgumentException {
        Vect.check(ax, ay);
        Vect.check(bx, by);
        if ((ax == bx) && (ay == by)) {
            throw new IllegalArgumentException("Points were the same! [" + ax + "," + ay + "," + bx + "," + by + "]");
        }
    }

    /**
     * Get ax
     *
     * @return
     */
    public double getAx() {
        return ax;
    }

    /**
     * Get ay
     *
     * @return
     */
    public double getAy() {
        return ay;
    }

    /**
     * Get bx
     *
     * @return
     */
    public double getBx() {
        return bx;
    }

    /**
     * Get by
     *
     * @return
     */
    public double getBy() {
        return by;
    }

    /**
     * Get a
     *
     * @return
     */
    @Transient
    public Vect getA() {
        return new Vect(ax, ay);
    }

    /**
     * Get b
     *
     * @return
     */
    @Transient
    public Vect getB() {
        return new Vect(bx, by);
    }

    /**
     * Get A
     *
     * @param target target in which to place A
     * @return target
     */
    public VectBuilder getA(VectBuilder target) {
        target.set(ax, ay);
        return target;
    }

    /**
     * Get B
     *
     * @param target target in which to place B
     * @return target
     */
    public VectBuilder getB(VectBuilder target) {
        target.set(bx, by);
        return target;
    }

    /**
     * Get the mid point
     *
     * @return
     */
    public Vect getMid() {
        return new Vect((ax + bx) / 2, (ay + by) / 2);
    }

    /**
     * Get the mid point
     *
     * @param target target in which to place mid point
     * @return target
     */
    public VectBuilder getMid(VectBuilder target) {
        return target.set((ax + bx) / 2, (ay + by) / 2);
    }

    /**
     * Determine if the distance between the end points is greater than the tolerance given
     *
     * @param tolerance
     * @return true if distance is greater, false otherwise
     */
    public boolean isValid(Tolerance tolerance) {
        return (!tolerance.match(ax, ay, bx, by));
    }

    /**
     * If b is less than A when compared as a Vect, return a version of this line where A and B are
     * swapped, otherwise return this
     *
     * @return a line.
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
     * Get the square of the length of the line (faster than getting the length, and sufficient for
     * some operations)
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
     * Get the side of the line on which vect lies. Positive values imply that it lies on the left
     * of the line. Negative values imply it lies on the right of the line. 0 implies it lies on the
     * line
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
     * Get the side of the line on which vect lies. Positive values imply that it lies on the left
     * of the line. Negative values imply it lies on the right of the line. 0 implies it lies on the
     * line
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
     * Project the value given along the line, where 0 represents A and 1 represents B. Tolerance is
     * used to snap points to A or B if close enough. Return true if such snapping occurs, or u > 0
     * or u < 1.
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

    static boolean project(double ax, double ay, double bx, double by, double u, Tolerance tolerance, VectBuilder target) {
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
     * Project the value given along the line, where 0 represents A and 1 represents B. Tolerance is
     * used to snap points to A or B if close enough. Return true if such snapping occurs, or u > 0
     * or u < 1. Returned point is projected outward a dist distFromLine from the line, where
     * positive values are on the right, negative values are on the left
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

    static boolean projectOutward(double ax, double ay, double bx, double by, double u, double distFromLine, Tolerance tolerance, VectBuilder target) {
        boolean ret = project(ax, ay, bx, by, u, tolerance, target);
        double dx = bx - ax;
        double dy = by - ay;
        double len = Math.sqrt(dx * dx + dy * dy);
        double mul = Math.abs(distFromLine) / len;
        dx *= mul;
        dy *= mul;
        if (distFromLine < 0) {
            target.add(-dy, dx);
        } else {
            target.add(dy, -dx);
        }
        return ret;
    }

    /**
     * Project the value given along the line segment, where 0 represents A and 1 represents B.
     * Tolerance is used to snap points to A or B if close enough. If u < 0 return A and if u > 1
     * return B and the point is beyond the tolerance, return null
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
     * Get the point on the line closest to the point given
     *
     * @param vect
     * @param tolerance
     * @param target
     * @throws NullPointerException
     */
    public void projectOnToLine(Vect vect, Tolerance tolerance, VectBuilder target) throws NullPointerException {
        projectOnToLineInternal(ax, ay, bx, by, vect.x, vect.y, tolerance, target);
    }

    static void projectOnToLineInternal(double ax, double ay, double bx, double by, double x, double y, Tolerance tolerance, VectBuilder target) {
        double jax = x;
        double jay = y;
        double jbx = x + (by - ay);
        double jby = y - (bx - ax);
        intersectionLineInternal(ax, ay, bx, by, jax, jay, jbx, jby, Tolerance.DEFAULT, target);
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
        return distLineVectSq(ax, ay, bx, by, vect.getX(), vect.getY());
    }

    /**
     * Get the square of the distance from the line given to the point given
     *
     * @param ax
     * @param ay
     * @param bx
     * @param by
     * @param x
     * @param y
     * @return
     */
    public static double distLineVectSq(double ax, double ay, double bx, double by, double x, double y) {
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
        return distSegVectSq(ax, ay, bx, by, vect.getX(), vect.getY());
    }

    /**
     * Get the distance between a line segment and a vector
     *
     * @param ax
     * @param ay
     * @param bx
     * @param by
     * @param x
     * @param y
     * @return
     */
    public static double distSegVectSq(double ax, double ay, double bx, double by, double x, double y) {
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
     * Determine if this line is parallell to that given, with differences in slope within the
     * tolerance
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
     * Determine if segments intersect. Segments which do not intersect, but are within the
     * tolerance given from each other are considered to intersect unless they are parallel
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
     * Get the intersection of this line and that given. Lines which do not intersect, but are
     * within the tolerance given from each other are considered to intersect at their end points
     * unless they are parallel
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

        double x, y;
        if (ax == bx) {
            x = ax;
        } else if (jax == jbx) {
            x = jax;
        } else {
            x = (ui * (bx - ax)) + ax;
        }
        if (ay == by) {
            y = ay;
        } else if (jay == jby) {
            y = jay;
        } else {
            y = (ui * (by - ay)) + ay;
        }

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
     * Get the intersection of this segment and that given. Segments which do not intersect, but are
     * within the tolerance given from each other are considered to intersect at their end points
     * unless they are parallel
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

        double x, y;
        if (ax == bx) {
            x = ax;
        } else if (jax == jbx) {
            x = jax;
        } else {
            x = (ui * (bx - ax)) + ax;
        }
        if (ay == by) {
            y = ay;
        } else if (jay == jby) {
            y = jay;
        } else {
            y = (ui * (by - ay)) + ay;
        }

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

    static void intersectionLineCircleInternal(double ax, double ay, double bx, double by,
            double cx, double cy, double radius, Tolerance tolerance, VectBuilder working, VectList intersections) {

        projectOnToLineInternal(ax, ay, bx, by, cx, cy, tolerance, working); //get the closest point on the line to the circle center

        double dx = working.getX();
        double dy = working.getY();

        double distToLineSq = Vect.distSq(cx, cy, dx, dy);
        double radiusSq = radius * radius;
        switch (tolerance.check(distToLineSq - radiusSq)) {
            case 1: // dist to line is greater than radius - line does not intersect circle
                return;
            case 0: // dist to line matches radius - line touches circle
                intersections.add(dx, dy);
                return;
            default: // dist to line is less than radius - line crosses circle
                double mx = bx - ax;
                double my = by - ay;
                double segmentLen = Math.sqrt(mx * mx + my * my);
                double projectDist;
                if (tolerance.check(distToLineSq) == 0) { // line crosses circle center
                    projectDist = radius;
                    dx = cx;
                    dy = cy;
                } else {
                    projectDist = Math.sqrt(radiusSq - distToLineSq);
                }
                double mul = projectDist / segmentLen;
                mx *= mul;
                my *= mul;
                intersections.add(dx - mx, dy - my);
                intersections.add(dx + mx, dy + my);
        }
    }

    @Override
    @Transient
    public Rect getBounds() {
        return new RectBuilder().addInternal(ax, ay).addInternal(bx, by).build();
    }

    @Override
    public Line transform(Transform transform) throws NullPointerException {
        Vect ta = transform.transform(getA());
        Vect tb = transform.transform(getB());
        return new Line(ta, tb);
    }

    @Override
    public PathIter iterator() {
        return new PathIter(){
            int state;
            
            @Override
            public boolean isDone() {
                return (state > 1);
            }

            @Override
            public void next() {
                state++;
            }

            @Override
            public PathSegType currentSegment(double[] coords) throws IllegalStateException {
                switch(state){
                    case 0:
                        coords[0] = ax;
                        coords[1] = ay;
                        return PathSegType.MOVE;
                    case 1:
                        coords[0] = bx;
                        coords[1] = by;
                        return PathSegType.LINE;
                    default:
                        throw new IllegalStateException();
                }
                
            }      
        };
    }

    /**
     * Convert this line to a line string containing 1 line.
     *
     * @return a line string
     */
    public LineString toLineString() {
        return new LineString(new VectList(ax, ay, bx, by));
    }

    @Override
    public GeoShape toGeoShape(Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        LineSet lines = new LineSet(new LineString[]{toLineString()});
        GeoShape ret = new GeoShape(null, lines, null, null);
        return ret;
    }

    @Override
    public void addTo(Network network, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        network.addLinkInternal(ax, ay, bx, by);
    }

    /**
     * Determine if the triangle formed by the points a, vect, b is counterclockwise
     *
     * @param vect vector
     * @param accuracy
     * @return 1 if counter clockwise, -1 if clockwise, 0 if point is on line segment ab
     */
    public int counterClockwise(Vect vect, Tolerance accuracy) throws NullPointerException {
        return counterClockwise(ax, ay, bx, by, vect.x, vect.y, accuracy);
    }

    /**
     * Determine if the triangle formed by the points a, vect, b is counterclockwise
     *
     * @param vect vector
     * @param accuracy
     * @return 1 if counter clockwise, -1 if clockwise, 0 if point is on line segment ab
     */
    public int counterClockwise(VectBuilder vect, Tolerance accuracy) throws NullPointerException {
        return counterClockwise(ax, ay, bx, by, vect.getX(), vect.getY(), accuracy);
    }

    /**
     * Determine if the point given lies to the left, right, or on the line given.
     *
     * @param ax
     * @param ay
     * @param bx
     * @param by
     * @param x
     * @param y
     * @param accuracy
     * @return 1 if left of line, -1 if right of line, 0 if on line.
     */
    public static int counterClockwise(double ax, double ay, double bx, double by, double x, double y, Tolerance accuracy) {
        bx -= ax;
        by -= ay;
        x -= ax;
        y -= ay;
        double ccw = x * by - y * bx;
        if (ccw == 0.0) { // Point is colinear - classify based on whether b is further away from a than point
            ccw = ((x * x) + (y * y)) - ((bx * bx) + (by * by));
            ccw = Math.max(ccw, 0);
        }
        return accuracy.check(ccw);
    }

    @Override
    public Geom buffer(double amt, Linearizer linearizer, Tolerance tolerance) throws IllegalArgumentException, NullPointerException {
        if (amt == 0) {
            return this;
        } else if (amt < 0) {
            return null;
        }
        VectList result = new VectList();
        VectBuilder vect = new VectBuilder();

        projectOutward(0, -amt, tolerance, vect);
        double ix = vect.getX();
        double iy = vect.getY();
        result.add(ix, iy);
        projectOutward(0, amt, tolerance, vect);
        linearizer.linearizeSegment(ax, ay, ix, iy, vect.getX(), vect.getY(), result);

        projectOutward(1, amt, tolerance, vect);
        double jx = vect.getX();
        double jy = vect.getY();
        result.add(jx, jy);
        projectOutward(1, -amt, tolerance, vect);
        linearizer.linearizeSegment(bx, by, jx, jy, vect.getX(), vect.getY(), result);

        result.add(ix, iy);

        return new Area(new Ring(result, null));
    }

    @Override
    public int relate(Vect vect, Tolerance tolerance) throws NullPointerException {
        return (distSegVectSq(vect) <= (tolerance.toleranceSq)) ? (Relation.TOUCH | Relation.A_OUTSIDE_B) : Relation.DISJOINT;
    }

    @Override
    public int relate(VectBuilder vect, Tolerance tolerance) throws NullPointerException {
        return (distSegVectSq(ax, ay, bx, by, vect.getX(), vect.getY()) <= tolerance.toleranceSq)
                ? (Relation.TOUCH | Relation.A_OUTSIDE_B) : Relation.DISJOINT;
    }

    @Override
    public int relate(Geom geom, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        return NetworkRelationProcessor.relate(this, geom, linearizer, accuracy);
    }

    @Override
    public double getArea(Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        return 0;
    }

    @Override
    public Geom union(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        return toLineString().union(other, linearizer, accuracy);
    }

    @Override
    public Geom intersection(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        if (getBounds().relate(other.getBounds(), accuracy) == Relation.DISJOINT) {
            return null;
        }
        return toLineString().intersection(other, linearizer, accuracy);
    }

    @Override
    public Geom less(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        if (getBounds().relate(other.getBounds(), accuracy) == Relation.DISJOINT) {
            return this;
        }
        return toLineString().less(other, linearizer, accuracy);
    }

    @Override
    public Geom xor(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        return toLineString().xor(other, linearizer, accuracy);
    }

    @Override
    public int compareTo(Line other) {
        return compare(ax, ay, bx, by, other.ax, other.ay, other.bx, other.by);
    }

    /**
     * Get a line segment based on this line where both end points are guaranteed to be outside the
     * bounds given, and the line will cross the bounds so long as they are not disjoint
     *
     * @param bounds
     * @return crossing segment, or null if line is disjoint from bounds
     * @throws NullPointerException if bounds was null
     */
    public Line segCrossing(Rect bounds) throws NullPointerException {
        double dx = bx - ax;
        double dy = by - ay;
        double dydx = dy / dx;
        double ix, iy, jx, jy;
        if ((dydx >= -1) && (dydx <= 1)) { // line is more horizontal - calculate y for x values
            ix = bounds.minX - 1;
            iy = dydx * (ix - ax) + ay;
            jx = bounds.maxX + 1;
            jy = dydx * (jx - ax) + ay;
            double minY = Math.min(iy, jy);
            double maxY = Math.max(iy, jy);
            if ((minY > bounds.maxY) || (maxY < bounds.minY)) {
                return null;
            }
        } else { // line is more vertical - calculate x for y values
            double dxdy = dx / dy;
            iy = bounds.minY - 1;
            ix = dxdy * (iy - ay) + ax;
            jy = bounds.maxY + 1;
            jx = dxdy * (jy - ay) + ax;
            double minX = Math.min(ix, jx);
            double maxX = Math.max(ix, jx);
            if ((minX > bounds.maxX) || (maxX < bounds.minX)) {
                return null;
            }
        }
        return new Line(ix, iy, jx, jy);
    }

    /**
     * Compare the 2 lines given. Assumes that a and b in each lines have standard vector ordering.
     *
     * @param iax
     * @param iay
     * @param ibx
     * @param iby
     * @param jax
     * @param jay
     * @param jbx
     * @param jby
     * @return
     */
    public static int compare(double iax, double iay, double ibx, double iby,
            double jax, double jay, double jbx, double jby) {
        int ret = Vect.compare(iax, iay, jax, jay);
        if (ret == 0) {
            ret = Vect.compare(ibx, iby, jbx, jby);
        }
        return ret;
    }

    @Override
    public int hashCode() {
        return hashCode(ax, ay, bx, by);
    }

    /**
     * Get a hashcode for the line given
     *
     * @param ax
     * @param ay
     * @param bx
     * @param by
     * @return
     */
    public static int hashCode(double ax, double ay, double bx, double by) {
        int hash = 7;
        hash = 97 * hash + Vect.hash(ax);
        hash = 97 * hash + Vect.hash(ay);
        hash = 97 * hash + Vect.hash(bx);
        hash = 97 * hash + Vect.hash(by);
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
     * Convert this line to a string in the format [ax,ay,bx,by] and add it to the appendable given
     *
     * @param appendable
     * @throws GeomException if there was an output error
     * @throws NullPointerException if appendable was null
     */
    public void toString(Appendable appendable) throws GeomException, NullPointerException {
        try {
            appendable.append("[\"").append(CODE).append("\",")
                    .append(Vect.ordToStr(ax)).append(',')
                    .append(Vect.ordToStr(ay)).append(',')
                    .append(Vect.ordToStr(bx)).append(',')
                    .append(Vect.ordToStr(by)).append(']');
        } catch (IOException ex) {
            throw new GeomException("Error writing", ex);
        }
    }

    @Override
    public Line clone() {
        return this;
    }

}
