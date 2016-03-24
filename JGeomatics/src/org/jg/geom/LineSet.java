package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.IOException;
import java.util.Arrays;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.VectList;

/**
 * Immutable set of 2D lines. Checks are in place to insure that no ordinates are NaN or Infinite,
 * and that no line segment crosses or lies on another
 *
 * @author tofar
 */
public class LineSet implements Geom {

    LineString[] lineStrings;

    Rect bounds;

    LineSet(LineString... lineStrings) {
        this.lineStrings = lineStrings;
    }

    static LineSet valueOfInternal(Network network) throws NullPointerException {
        LineString[] lineStrings = LineString.parseAllInternal(network);
        return (lineStrings.length == 0) ? null : new LineSet(lineStrings);
    }

    /**
     * Get all line strings from the network given as a line set. Intersections are added,
     * unconnected points are ignored, and rings are treated as line strings
     *
     * @param accuracy
     * @param network
     * @return a line set, or null if there were no lines
     * @throws NullPointerException if accuracy or network was null
     */
    public static LineSet valueOf(Tolerance accuracy, Network network) throws NullPointerException {
        network = network.clone();
        network.explicitIntersections(accuracy);
        network.snap(accuracy);
        return valueOfInternal(network);
    }

    /**
     * Convert the vectList given to a line set. Intersections are added, unconnected points are
     * ignored, and rings are treated as line strings
     *
     * @param accuracy
     * @param vects
     * @return a line set, or null if there were no lines
     * @throws NullPointerException if accuracy or vects was null
     */
    public static LineSet valueOf(Tolerance accuracy, VectList vects) throws NullPointerException {
        Network network = new Network();
        network.addAllLinks(vects);
        network.explicitIntersections(accuracy);
        network.snap(accuracy);
        return valueOfInternal(network);
    }

    /**
     * Convert the ordinates given to a line set. Intersections are added, unconnected points are
     * ignored, and rings are treated as line strings
     *
     * @param accuracy
     * @param ords
     * @return a line set, or null if there were no lines
     * @throws NullPointerException if accuracy or ords was null
     * @throws IllegalArgumentException if an ordinate was infinite or NaN
     */
    public static LineSet valueOf(Tolerance accuracy, double... ords) throws NullPointerException, IllegalArgumentException {
        return valueOf(accuracy, new VectList(ords));
    }

    /**
     * Get the number of line strings
     *
     * @return
     */
    public int numLineStrings() {
        return lineStrings.length;
    }

    /**
     * Get the line string at the index given
     *
     * @param index
     * @return
     * @throws IndexOutOfBoundsException if index was out of bounds
     */
    public LineString getLineString(int index) throws IndexOutOfBoundsException {
        return lineStrings[index];
    }

    @Override
    public Rect getBounds() {
        Rect ret = bounds;
        if (ret == null) {
            RectBuilder builder = new RectBuilder();
            for (LineString lineString : lineStrings) {
                builder.add(lineString.getBounds());
            }
            ret = builder.build();
            bounds = ret;
        }
        return ret;
    }

    @Override
    public LineSet transform(Transform transform) throws NullPointerException {
        if (transform.mode == Transform.NO_OP) {
            return this;
        }
        LineString[] transformed = new LineString[lineStrings.length];
        for (int i = transformed.length; i-- > 0;) {
            transformed[i] = lineStrings[i].transform(transform);
        }
        Arrays.sort(transformed, COMPARATOR);
        return new LineSet(transformed);
    }

    public Geom simplify() {
        return (lineStrings.length == 1) ? lineStrings[0].simplify() : this;
    }

    @Override
    public PathIterator pathIterator() {
        PathIterator iter = new PathIterator() {
            PathIterator iter = lineStrings[0].pathIterator();
            int index = 0;

            @Override
            public int getWindingRule() {
                return WIND_NON_ZERO;
            }

            @Override
            public boolean isDone() {
                return iter == null;
            }

            @Override
            public void next() {
                if (iter != null) {
                    iter.next();
                    if (iter.isDone()) {
                        index++;
                        if (index < lineStrings.length) {
                            iter = lineStrings[index].pathIterator();
                        } else {
                            iter = null;
                        }
                    }
                }
            }

            @Override
            public int currentSegment(float[] coords) {
                return iter.currentSegment(coords);
            }

            @Override
            public int currentSegment(double[] coords) {
                return iter.currentSegment(coords);
            }
        };
        return iter;
    }

    @Override
    public LineSet clone() {
        return this;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        toString(str);
        return str.toString();
    }

    @Override
    public void toString(Appendable appendable) throws NullPointerException, GeomException {
        try {
            appendable.append("[\"LT\"");
            for (LineString lineString : lineStrings) {
                appendable.append(", ");
                lineString.vects.toString(appendable);
            }
            appendable.append(']');
        } catch (IOException ex) {
            throw new GeomException("Error writing MultiLineString", ex);
        }
    }

    @Override
    public GeoShape toGeoShape(Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        return toGeoShape();
    }

    /**
     * Convert to GeoShape
     *
     * @return
     */
    public GeoShape toGeoShape() {
        return new GeoShape(null, this, null);
    }

    @Override
    public void addTo(Network network, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        addTo(network);
    }

    /**
     * Add to network
     *
     * @param network
     */
    public void addTo(Network network) {
        for (LineString lineString : lineStrings) {
            lineString.addTo(network);
        }
    }

    @Override
    public Geom buffer(double amt, Tolerance flatness, Tolerance accuracy) throws IllegalArgumentException, NullPointerException {
        if (amt < 0) {
            return null;
        } else if (amt == 0) {
            return this;
        }
        Network network = new Network();
        for (LineString lineString : lineStrings) {
            VectList buffer = LineString.bufferInternal(lineString.vects, amt, flatness, accuracy);
            network.addAllLinks(buffer);
        }
        network.explicitIntersections(accuracy);
        network.snap(accuracy);
        double tol = amt - (flatness.tolerance + accuracy.tolerance);
        for (LineString lineString : lineStrings) {
            LineString.removeNearLines(network, lineString.vects, tol);
        }
        return Area.valueOfInternal(accuracy, network);
    }

    @Override
    public Relate relate(Vect vect, Tolerance accuracy) throws NullPointerException {
        return relateInternal(vect.x, vect.y, accuracy);
    }

    @Override
    public Relate relate(VectBuilder vect, Tolerance accuracy) throws NullPointerException {
        return relateInternal(vect.getX(), vect.getY(), accuracy);
    }

    Relate relateInternal(double x, double y, Tolerance accuracy) {
        if (getBounds().relateInternal(x, y, accuracy) == Relate.OUTSIDE) {
            return Relate.OUTSIDE;
        }
        for (LineString lineString : lineStrings) {
            if (lineString.relateInternal(x, y, accuracy) == Relate.TOUCH) {
                return Relate.TOUCH;
            }
        }
        return Relate.OUTSIDE;
    }

    @Override
    public Geom union(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        if (other instanceof LineString) {
            return ((LineString) other).unionLineSet(this, accuracy).simplify();
        } else if (other instanceof LineSet) {
            return union((LineSet) other, accuracy).simplify();
        } else {
            return union(other.toGeoShape(flatness, accuracy), accuracy).simplify();
        }
    }

    /**
     * Get the union of this line set and that given
     *
     * @param other
     * @param accuracy
     * @return a line set
     * @throws NullPointerException if other or accuracy was null
     */
    public LineSet union(LineSet other, Tolerance accuracy) throws NullPointerException {
        if (getBounds().isDisjoint(other.getBounds())) {
            LineString[] lines = new LineString[numLineStrings() + other.numLineStrings()];
            System.arraycopy(lineStrings, 0, lines, 0, lineStrings.length);
            System.arraycopy(other.lineStrings, 0, lines, lineStrings.length, other.lineStrings.length);
            Arrays.sort(lines, COMPARATOR);
            return new LineSet(lines);
        }
        Network network = new Network();
        addTo(network);
        other.addTo(network);
        network.explicitIntersections(accuracy);
        LineString[] ret = LineString.parseAllInternal(network);
        return new LineSet(ret);
    }

    /**
     * Get the union of this line set and the GeoShape given
     *
     * @param other
     * @param accuracy
     * @return a GeoShape
     * @throws NullPointerException if other or accuracy was null
     */
    public GeoShape union(GeoShape other, Tolerance accuracy) throws NullPointerException {
        if (other.getBounds().isDisjoint(getBounds(), accuracy)) { // quick way - disjoint
            LineSet _lines;
            if (other.lines == null) {
                _lines = this;
            } else {
                LineString[] lines = new LineString[numLineStrings() + other.lines.numLineStrings()];
                System.arraycopy(lineStrings, 0, lines, 0, lineStrings.length);
                System.arraycopy(other.lines.lineStrings, 0, lines, lineStrings.length, other.lines.numLineStrings());
                Arrays.sort(lines, COMPARATOR);
                _lines = new LineSet(lines);
            }
            return new GeoShape(other.area, _lines, other.points);
        }

        Network network = new Network();
        addTo(network);
        if (other.lines != null) {
            other.lines.addTo(network);
            network.explicitIntersections(accuracy);
        }
        Area area = other.area;
        if (area != null) {
            network.explicitIntersectionsWith(area.getLineIndex(), accuracy);
            VectBuilder workingVect = new VectBuilder();
            network.removeInsideOrOutsideInternal(area, accuracy, Relate.INSIDE, workingVect);
            network.removeTouchingInternal(area, accuracy, workingVect);
        }
        LineSet lines = LineSet.valueOfInternal(network);

        PointSet points = other.points;
        if (points != null) {
            points = points.less(this, accuracy);
        }
        return new GeoShape(area, lines, points);
    }

    @Override
    public Geom intersection(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        if (other.getBounds().isDisjoint(getBounds(), accuracy)) { // quick way - disjoint
            return null;
        } else {
            return intersection(other.toGeoShape(flatness, accuracy), accuracy).simplify();
        }
    }

    /**
     * Get the intersection of this LineSet and the GeoShape given
     *
     * @param other
     * @param accuracy
     * @return a GeoShape or null if there was no intersection
     * @throws NullPointerException if other or accuracy was null
     */
    public GeoShape intersection(GeoShape other, Tolerance accuracy) throws NullPointerException {
        if (other.getBounds().isDisjoint(getBounds(), accuracy)) { // quick way - disjoint
            return null;
        }
        Network network = new Network();
        addTo(network);
        other.addTo(network);
        network.explicitIntersections(accuracy);
        network.snap(accuracy);
        VectBuilder workingVect = new VectBuilder();
        network.removeInsideOrOutsideInternal(this, accuracy, Relate.OUTSIDE, workingVect);
        network.removeInsideOrOutsideInternal(other, accuracy, Relate.OUTSIDE, workingVect);
        LineSet lines = valueOfInternal(network);
        PointSet points = PointSet.valueOf(network, accuracy);
        if ((lines == null) && (points == null)) {
            return null; // no intersection
        }
        return new GeoShape(null, lines, points);
    }

    @Override
    public Geom less(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        if (other.getBounds().isDisjoint(getBounds(), accuracy)) { // quick way - disjoint
            return simplify();
        }
        LineSet ret = less(other.toGeoShape(flatness, accuracy), accuracy);
        if ((ret != null) && (ret.numLineStrings() == 1)) {
            return ret.getLineString(0);
        }
        return ret;
    }

    /**
     * Get the lines in this LineSet which are not in the GeoShape given
     *
     * @param other
     * @param accuracy
     * @return a LineSet or null if other covered this
     * @throws NullPointerException if other or accuracy was null
     */
    public LineSet less(GeoShape other, Tolerance accuracy) throws NullPointerException {
        if (other.getBounds().isDisjoint(getBounds(), accuracy)) { // quick way - disjoint
            return this;
        }
        Network network = new Network();
        addTo(network);
        other.addTo(network);
        network.explicitIntersections(accuracy);
        network.snap(accuracy);
        VectBuilder workingVect = new VectBuilder();
        network.removeInsideOrOutsideInternal(other, accuracy, Relate.INSIDE, workingVect);
        network.removeTouchingInternal(other, accuracy, workingVect);
        LineSet lines = valueOfInternal(network);
        return lines;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Arrays.hashCode(this.lineStrings);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LineSet) {
            final LineSet other = (LineSet) obj;
            return Arrays.equals(lineStrings, other.lineStrings);
        }
        return false;
    }
}
