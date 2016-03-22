package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.VectList;

/**
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
        LineString[] lineStrings = LineString.valueOfInternal(network);
        return (lineStrings.length == 0) ? null : new LineSet(lineStrings);
    }
    
    public static LineSet valueOf(Network network, Tolerance accuracy){
        network = network.clone();
        network.explicitIntersections(accuracy);
        return valueOfInternal(network);
    }
    
    public static LineSet valueOf(VectList vects, Tolerance accuracy){
        Network network = new Network();
        network.addAllLinks(vects);
        network.explicitIntersections(accuracy);
        return valueOfInternal(network);
    }
    
    public int numLines() {
        return lineStrings.length;
    }

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
    
    public Geom simplify(){
        return (lineStrings.length == 1) ? lineStrings[0].simplify() : this;
    }

    @Override
    public PathIterator pathIterator() {
        PathIterator iter = new PathIterator() {
            PathIterator iter;
            int index = -1;

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
                    if (iter.isDone()) {
                        iter.next();
                    }
                    if (iter.isDone()) {
                        iter = null;
                    }
                }
                while (iter == null) {
                    index++;
                    if (index < lineStrings.length) {
                        iter = lineStrings[index].pathIterator();
                    } else {
                        return;
                    }
                    if (iter.isDone()) {
                        iter = null;
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
        iter.next();
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

    public GeoShape toGeoShape() {
        return new GeoShape(null, this, null);
    }

    @Override
    public void addTo(Network network, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        addTo(network);
    }

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
        for (LineString lineString : lineStrings) {
            LineString.removeNearLines(network, lineString.vects, amt);
        }
        return Area.valueOfInternal(network, accuracy);
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
            return ((LineString) other).union(this, accuracy);
        } else if (other instanceof LineSet) {
            return union((LineSet) other, accuracy);
        } else {
            return union(other.toGeoShape(flatness, accuracy), accuracy);
        }
    }

    public LineSet union(LineSet other, Tolerance accuracy) throws NullPointerException {
        if (getBounds().isDisjoint(other.getBounds())) {
            LineString[] lines = new LineString[numLines() + other.numLines()];
            System.arraycopy(lineStrings, 0, lines, 0, lineStrings.length);
            System.arraycopy(other.lineStrings, 0, lines, lineStrings.length, other.lineStrings.length);
            Arrays.sort(lines, COMPARATOR);
            return new LineSet(lines);
        }
        Network network = new Network();
        addTo(network);
        other.addTo(network);
        network.explicitIntersections(accuracy);
        LineString[] ret = LineString.valueOfInternal(network);
        if (ret.length == 0) {
            return null;
        } else {
            return new LineSet(ret);
        }
    }

    public GeoShape union(GeoShape other, Tolerance accuracy) throws NullPointerException {
        if (other.getBounds().isDisjoint(getBounds(), accuracy)) { // quick way - disjoint
            LineSet _lines;
            if(other.lines == null){
                _lines = this;
            }else{
                LineString[] lines = new LineString[numLines() + other.lines.numLines()];
                System.arraycopy(lineStrings, 0, lines, 0, lineStrings.length);
                System.arraycopy(other.lines.lineStrings, 0, lines, lineStrings.length, other.lines.numLines());
                Arrays.sort(lines, COMPARATOR);
                _lines = new LineSet(lines);
            }
            return new GeoShape(other.area, _lines, other.points);
        }

        Network network = new Network();
        addTo(network);
        if(other.lines != null){
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
        LineSet lines = new LineSet(LineString.valueOfInternal(network));

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
            return this;
        }
        LineSet ret = less(other.toGeoShape(flatness, accuracy), accuracy);
        if ((ret != null) && (ret.numLines() == 1)) {
            return ret.getLineString(0);
        }
        return ret;
    }

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
        if(obj instanceof LineSet){
            final LineSet other = (LineSet) obj;
            return Arrays.equals(lineStrings, other.lineStrings);
        }
        return true;
    }
    
    
}
