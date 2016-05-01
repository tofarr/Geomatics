package org.jg.geom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jg.geom.Network.LinkProcessor;
import org.jg.geom.Network.VertexProcessor;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.VectList;

/**
 * Immutable geometry combining Area,LineSet and PointSet. Any of these may be null, though at least
 * one of these should be set. Checks are in place to insure that points are disjoint from lines and
 * areas, and that no part of any line may be inside an area, and lines may only touch areas at
 * their end points.
 *
 * @author tofarrell
 */
public class GeoShape implements Geom {

    public static final String CODE = "GS";
    /**
     * Area for this geom (may be null)
     */
    public final Area area;
    /**
     * Line strings (may be null) - no part may be inside area, and may only touch at the end point
     * of a line.
     */
    public final LineSet lines;
    /**
     * Points (may be null) - must be disjoint from area and line
     */
    public final PointSet points;
    // Bounds for this geo shape 
    Rect bounds;

    GeoShape(Area area, LineSet lines, PointSet points, Rect bounds) {
        this(area, lines, points);
        this.bounds = bounds;
    }

    GeoShape(Area area, LineSet lines, PointSet points) throws NullPointerException {
        if ((area == null) && (lines == null) && (points == null)) {
            throw new NullPointerException("Must set at least one of area, lines, or points");
        }
        this.area = area;
        this.lines = lines;
        this.points = points;
    }

    /**
     * Get a GeoShape based on the components given.
     *
     * @param area
     * @param lines
     * @param points
     * @param accuracy
     * @return
     * @throws NullPointerException if all components were null, or accuracy was null
     */
    public static GeoShape valueOf(Area area, LineSet lines, PointSet points, Tolerance accuracy) throws NullPointerException {
        if ((lines != null) && (area != null)) {
            lines = lines.less(area, Linearizer.DEFAULT, accuracy);
        }
        if ((points != null) && (area != null)) {
            points = points.less(area, accuracy);
        }
        if ((points != null) && (lines != null)) {
            points = points.less(lines, accuracy);
        }
        return new GeoShape(area, lines, points);
    }

    /**
     * Get a geoshape based on the network given
     *
     * @param network
     * @param accuracy
     * @return
     * @throws NullPointerException if network or accuracy was null
     */
    public static GeoShape valueOf(Network network, Tolerance accuracy) throws NullPointerException {
        network.clone();
        network.explicitIntersections(accuracy);
        network.snap(accuracy);
        return valueOfInternal(network, accuracy);
    }

    static GeoShape valueOfInternal(Network network, Tolerance accuracy) {
        if (network.numVects() == 0) {
            return null;
        }
        VectList pointVects = new VectList();
        network.extractPoints(pointVects);
        List<VectList> linePaths = new ArrayList<>();
        network.extractHangLines(linePaths);
        Area area = Area.valueOfInternal(accuracy, network);
        if (area != null) { // validate points and lines against area

            for (int p = pointVects.size(); p-- > 0;) {
                if (!Relation.isDisjoint(area.relateInternal(pointVects.getX(p), pointVects.getY(p), accuracy))) {
                    pointVects.remove(p);
                }
            }
            Network lineNetwork = new Network();
            for (VectList linePath : linePaths) {
                double ax = linePath.getX(0);
                double ay = linePath.getY(0);
                for (int b = 1; b < linePath.size(); b++) {
                    double bx = linePath.getX(b);
                    double by = linePath.getY(b);
                    double x = (ax + bx) / 2;
                    double y = (ay + by) / 2;
                    if (Relation.isDisjoint(area.relateInternal(x, y, accuracy))) {
                        lineNetwork.addLinkInternal(ax, ay, bx, by);
                    }
                }
            }
            linePaths.clear();
            lineNetwork.extractHangLines(linePaths);
        }
        PointSet points = null;
        if (!pointVects.isEmpty()) {
            pointVects.sort();
            points = new PointSet(pointVects);
        }
        LineSet lines = null;
        if (!linePaths.isEmpty()) {
            LineString[] lineStrings = new LineString[linePaths.size()];
            for (int i = lineStrings.length; i-- > 0;) {
                lineStrings[i] = new LineString(linePaths.get(i));
            }
            lines = new LineSet(lineStrings);
        }
        return new GeoShape(area, lines, points);
    }

    @Override
    public Rect getBounds() {
        Rect ret = bounds;
        if (ret == null) {
            RectBuilder builder = new RectBuilder();
            if (area != null) {
                builder.add(area.getBounds());
            }
            if (lines != null) {
                builder.add(lines.getBounds());
            }
            if (points != null) {
                builder.add(points.getBounds());
            }
            ret = builder.build();
            bounds = ret;
        }
        return ret;
    }

    @Override
    public Geom transform(Transform transform) throws NullPointerException {
        Area _area = (area == null) ? null : area.transform(transform);
        LineSet _lines = (lines == null) ? null : lines.transform(transform);
        PointSet _points = (points == null) ? null : points.transform(transform);
        return new GeoShape(_area, _lines, _points);
    }

    @Override
    public PathIter iterator() {
        return new PathIter() {
            int state = nextPathIterator(-1);
            PathIter iter;

            @Override
            public boolean isDone() {
                return (iter == null);
            }

            @Override
            public void next() {
                if (iter.isDone()) {
                    nextPathIterator(state);
                } else {
                    iter.next();
                }
            }

            @Override
            public PathSegType currentSegment(double[] coords) {
                return iter.currentSegment(coords);
            }

            int nextPathIterator(int state) {
                state++;
                Geom geom;
                switch (state) {
                    case 0:
                        geom = area;
                        break;
                    case 1:
                        geom = lines;
                        break;
                    case 2:
                        geom = points;
                        break;
                    default:
                        geom = null;
                }
                if ((geom == null) && (state < 3)) {
                    return nextPathIterator(state);
                }
                iter = geom.iterator();
                return state;
            }

        };
    }

    /**
     * Add this GeoShape to the network given
     *
     * @param network
     */
    public void addTo(Network network) {
        if (area != null) {
            area.addTo(network);
        }
        if (lines != null) {
            lines.addTo(network);
        }
        if (points != null) {
            points.addTo(network);
        }
    }

    @Override
    public void addTo(Network network, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        addTo(network);
    }

    @Override
    public GeoShape clone() {
        return this;
    }

    @Override
    public void toString(Appendable appendable) throws NullPointerException, GeomException {
        try {
            appendable.append("[\"").append(CODE).append('"');
            if (area == null) {
                appendable.append("null");
            }else{
                area.toString(appendable);
            }
            appendable.append(',');
            if (lines == null) {
                appendable.append("null");
            }else{
                lines.toString(appendable);
            }
            appendable.append(',');
            if (points != null) {
                appendable.append("null");
            }else{
                points.toString(appendable);
            }
            appendable.append(']');
        } catch (IOException ex) {
            throw new GeomException("Error writing", ex);
        }
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        toString(str);
        return str.toString();
    }

    @Override
    public GeoShape toGeoShape(Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        return this;
    }

    @Override
    public Geom buffer(double amt, Linearizer linearizer, Tolerance tolerance) throws IllegalArgumentException, NullPointerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int relate(Vect vect, Tolerance tolerance) throws NullPointerException {
        return relateInternal(vect.x, vect.y, tolerance);
    }

    @Override
    public int relate(VectBuilder vect, Tolerance tolerance) throws NullPointerException {
        return relateInternal(vect.getX(), vect.getY(), tolerance);
    }

    int relateInternal(double x, double y, Tolerance tolerance) {
        int relate = Relation.NULL;
        if (points != null) {
            relate = points.relateInternal(x, y, tolerance);
            if (Relation.isTouch(relate)) {
                if ((lines != null) || (area != null)) { // in case single point
                    relate |= Relation.A_OUTSIDE_B;
                }
                return relate;
            }
        }
        if (lines != null) {
            relate = lines.relateInternal(x, y, tolerance);
            if (Relation.isTouch(relate)) {
                return relate;
            }
        }
        if (area != null) {
            return area.relateInternal(x, y, tolerance);
        }
        return Relation.DISJOINT;
    }

    @Override
    public int relate(Geom geom, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        return relate(geom.toGeoShape(linearizer, accuracy), accuracy);
    }

    public int relate(GeoShape geom, Tolerance accuracy) throws NullPointerException {
        int ret = Relation.NULL;
        if (area != null) {
            if (geom.area != null) {
                ret = area.relate(geom.area, accuracy);
            } else {
                ret |= Relation.A_OUTSIDE_B;
            }
        } else if (geom.area != null) {
            ret |= Relation.B_OUTSIDE_A;
        }
        if ((lines != null) || (points != null) || (geom.lines != null) || (geom.points != null)) {
            ret |= NetworkRelationProcessor.relate(this, geom, Linearizer.DEFAULT, accuracy);
        }
        return ret;
    }

    @Override
    public double getArea(Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        return (area == null) ? 0 : area.getArea();
    }

    @Override
    public Geom union(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        return union(other.toGeoShape(linearizer, accuracy), accuracy).simplify();
    }

    /**
     * Get the union of this geoshape and that given
     * @param other
     * @param accuracy
     * @return
     * @throws NullPointerException if other or accuracy was null
     */
    public GeoShape union(GeoShape other, Tolerance accuracy) throws NullPointerException {

        Area _area;
        if (area == null) {
            _area = other.area;
        } else if (other.area == null) {
            _area = area;
        } else {
            _area = area.union(other.area, accuracy);
        }

        LineSet _lines;
        if (lines == null) {
            _lines = other.lines;
        } else if (other.lines == null) {
            _lines = lines;
        } else {
            _lines = lines.union(other.lines, accuracy);
        }
        if ((_lines != null) && (_area != null)) {
            _lines = _lines.less(_area, Linearizer.DEFAULT, accuracy);
        }

        PointSet _points;
        if (points == null) {
            _points = other.points;
        } else if (other.points == null) {
            _points = points;
        } else {
            _points = points.union(other.points, accuracy);
        }
        if ((_points != null) && (_area != null)) {
            _points = _points.less(_area, accuracy);
        }
        if ((_points != null) && (_lines != null)) {
            _points = _points.less(_lines, accuracy);
        }

        return new GeoShape(_area, _lines, _points, getBounds().add(other.getBounds()));
    }

    @Override
    public Geom intersection(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        if (Relation.isDisjoint(getBounds().relate(other.getBounds(), accuracy))) {
            return null;
        }
        GeoShape ret = intersection(other.toGeoShape(linearizer, accuracy), accuracy);
        return (ret == null) ? null : ret.simplify();
    }
    
    /**
     * Get the intersection of this geoshape and that given
     * @param other
     * @param accuracy
     * @return
     * @throws NullPointerException if other or accuracy was null
     */
    public GeoShape intersection(final GeoShape other, final Tolerance accuracy) {
        if (Relation.isDisjoint(getBounds().relate(other.getBounds(), accuracy))) {
            return null;
        }

        final Network network = Network.valueOf(accuracy, Linearizer.DEFAULT, this, other);
        network.forEachVertex(new VertexProcessor() {
            @Override
            public boolean process(double x, double y, int numLinks) {
                if (Relation.isDisjoint(relateInternal(x, y, accuracy))
                        || Relation.isDisjoint(other.relateInternal(x, y, accuracy))) {
                    network.removeVertexInternal(x, y);
                }
                return true;
            }
        });
        network.forEachLink(new LinkProcessor() {
            @Override
            public boolean process(double ax, double ay, double bx, double by) {
                double x = (ax + bx) / 2;
                double y = (ay + by) / 2;
                if (Relation.isDisjoint(relateInternal(x, y, accuracy))
                        || Relation.isDisjoint(other.relateInternal(x, y, accuracy))) {
                    network.removeLinkInternal(ax, ay, bx, by);
                }
                return true;
            }

        });
        GeoShape ret = GeoShape.valueOfInternal(network, accuracy);
        return ret;
    }

    @Override
    public GeoShape less(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        if (Relation.isDisjoint(getBounds().relate(other.getBounds(), accuracy))) {
            return this;
        }
        return less(other.toGeoShape(linearizer, accuracy), accuracy);
    }
    
    /**
     * Get the remainder of this geoshape after that given is subtracted
     * @param other
     * @param accuracy
     * @return
     * @throws NullPointerException if other or accuracy was null
     */
    public GeoShape less(GeoShape other, final Tolerance accuracy) throws NullPointerException {
        if (Relation.isDisjoint(getBounds().relate(other.getBounds(), accuracy))) {
            return this;
        }
        //First we get an area if there was one...
        Area _area = area;
        if ((_area != null) && (other.area != null)) {
            _area = _area.less(other.area, accuracy);
        }
        LineSet _lines = null;
        if (lines != null) {
            _lines = lines.less(other, Linearizer.DEFAULT, accuracy);
        }
        PointSet _points = null;
        if (points != null) {
            _points = points.less(other, accuracy);
        }
        return (_area != null) || (_lines != null) || (_points != null) ? new GeoShape(_area, _lines, _points) : null;
    }

    @Override
    public GeoShape xor(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        return xor(other.toGeoShape(linearizer, accuracy), accuracy);
    }

    /**
     * Xor this geoshape with the geoshape given
     *
     * @param other
     * @param accuracy
     * @return
     * @throws NullPointerException
     */
    public GeoShape xor(GeoShape other, final Tolerance accuracy) throws NullPointerException {
        Area _area = area;
        if (other.area != null) {
            _area = (_area == null) ? other.area : _area.xor(other.area, accuracy);
        }

        LineSet _lines = lines;
        if (other.lines != null) {
            _lines = (_lines == null) ? other.lines : _lines.xor(other.lines, accuracy);
        }
        if ((_lines != null) && (area != null)) {
            _lines = _lines.less(area, Linearizer.DEFAULT, accuracy);
        }
        if ((_lines != null) && (other.area != null)) {
            _lines = _lines.less(other.area, Linearizer.DEFAULT, accuracy);
        }

        PointSet _points = points;
        if (other.points != null) {
            _points = (_points == null) ? other.points : _points.xor(other.points, accuracy);
        }
        if ((_points != null) && (area != null)) {
            _points = _points.less(area, accuracy);
        }
        if ((_points != null) && (other.area != null)) {
            _points = _points.less(other.area, accuracy);
        }

        if ((_points != null) && (lines != null)) {
            _points = _points.less(lines, accuracy);
        }
        if ((_points != null) && (other.lines != null)) {
            _points = _points.less(other.lines, accuracy);
        }

        return (_area != null) || (_lines != null) || (_points != null) ? new GeoShape(_area, _lines, _points) : null;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.area);
        hash = 59 * hash + Objects.hashCode(this.lines);
        hash = 59 * hash + Objects.hashCode(this.points);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GeoShape)) {
            return false;
        }
        final GeoShape other = (GeoShape) obj;
        return Objects.equals(this.area, other.area)
                && Objects.equals(this.lines, other.lines)
                && Objects.equals(this.points, other.points);
    }

    /**
     * Convert this geoshape to Well Known text
     * @return
     */
    public String toWkt() {
        StringBuilder str = new StringBuilder();
        toWkt(str);
        return str.toString();
    }

    public void toWkt(Appendable appendable) throws GeomException {
        try {
            if ((area == null) && (lines == null)) {
                toPointWkt(appendable);
            } else if ((area == null) && (points == null)) {
                toLineWkt(appendable);
            } else if ((lines == null) && (points == null)) {
                toPolygonWkt(appendable);
            } else {
                toCollectionWkt(appendable);
            }
        } catch (IOException ex) {
            throw new GeomException("Error writing WKT", ex);
        }
    }

    public Geom simplify() {
        if ((area == null) && (lines == null)) {
            return points.simplify();
        } else if ((area == null) && (points == null)) {
            return lines.simplify();
        } else if ((lines == null) && (points == null)) {
            return area.simplify();
        } else {
            return this;
        }
    }

    private void toPointWkt(Appendable appendable) throws IOException {
        if (points.numPoints() == 1) {
            toPointWkt(points.getX(0), points.getY(0), appendable);
        } else {
            appendable.append("MULTIPOINT");
            toVectsWkt(points.vects, appendable);
        }
    }

    private void toLineWkt(Appendable appendable) throws IOException {
        if (lines.numLineStrings() == 1) {
            appendable.append("LINESTRING");
            toVectsWkt(lines.getLineString(0).vects, appendable);
        } else {
            appendable.append("MULTILINESTRING(");
            for (int i = 0; i < lines.numLineStrings(); i++) {
                if (i != 0) {
                    appendable.append(", ");
                }
                toVectsWkt(lines.getLineString(i).vects, appendable);
            }
            appendable.append(")");
        }
    }

    private void toPolygonWkt(Appendable appendable) throws IOException {
        if ((area.shell != null) && (area.getDepth() < 3)) {
            toPolygonWkt(area, appendable);
        } else {
            appendable.append("MULTIPOLYGON(");
            if (area.shell != null) {
                toMultiPolygonWkt(area, appendable);
            } else {
                for (int i = 0; i < area.numChildren(); i++) {
                    if (i != 0) {
                        appendable.append(',');
                    }
                    toMultiPolygonWkt(area.getChild(i), appendable);
                }
            }
            appendable.append(')');

        }
    }

    private void toCollectionWkt(Appendable appendable) throws IOException {
        appendable.append("GEOMETRYCOLLECTION(");
        boolean comma = false;
        if (area != null) {
            if (area.shell == null) {
                for (int i = 0; i < area.numChildren(); i++) {
                    if (comma) {
                        appendable.append(',');
                    } else {
                        comma = true;
                    }
                    toPolygonWkt(area.getChild(i), appendable);
                }
            } else {
                toPolygonWkt(area, appendable);
            }
            comma = true;
        }
        if (lines != null) {
            for (int i = 0; i < lines.numLineStrings(); i++) {
                if (comma) {
                    appendable.append(',');
                } else {
                    comma = true;
                }
                appendable.append("LINESTRING");
                toVectsWkt(lines.getLineString(i).vects, appendable);
            }
        }
        if (points != null) {
            for (int i = 0; i < points.numPoints(); i++) {
                if (comma) {
                    appendable.append(',');
                } else {
                    comma = true;
                }
                toPointWkt(points.getX(i), points.getY(i), appendable);
            }
        }
        appendable.append(')');
    }

    private static void toPointWkt(double x, double y, Appendable appendable) throws IOException {
        appendable.append("POINT(").append(Vect.ordToStr(x)).append(' ').append(Vect.ordToStr(y)).append(')');
    }

    private static void toVectsWkt(VectList vects, Appendable appendable) throws IOException {
        appendable.append('(');
        for (int i = 0; i < vects.size(); i++) {
            if (i != 0) {
                appendable.append(", ");
            }
            appendable.append(Vect.ordToStr(vects.getX(i)))
                    .append(' ').append(Vect.ordToStr(vects.getY(i)));
        }
        appendable.append(')');
    }

    private static void toMultiPolygonWkt(Area area, Appendable appendable) throws IOException {
        appendable.append('(');
        toVectsWkt(area.shell.vects, appendable);
        for (int i = 0; i < area.numChildren(); i++) {
            appendable.append(',');
            toVectsWkt(area.getChild(i).shell.vects, appendable);
        }
        appendable.append(')');
        for (int i = 0; i < area.numChildren(); i++) {
            Area child = area.getChild(i);
            for (int j = 0; j < child.numChildren(); j++) {
                appendable.append(',');
                toMultiPolygonWkt(child.getChild(j), appendable);
            }
        }
    }

    private static void toPolygonWkt(Area area, Appendable appendable) throws IOException {
        appendable.append("POLYGON(");
        toVectsWkt(area.shell.vects, appendable);
        for (int i = 0; i < area.numChildren(); i++) {
            appendable.append(',');
            toVectsWkt(area.getChild(i).shell.vects, appendable);
        }
        appendable.append(')');
        for (int i = 0; i < area.numChildren(); i++) {
            Area child = area.getChild(i);
            for (int j = 0; j < child.numChildren(); j++) {
                appendable.append(',');
                toPolygonWkt(child.getChild(j), appendable);
            }
        }
    }

    void addIntersections(Network network, Tolerance accuracy) {
        if (area != null) {
            network.explicitIntersectionsWith(area.getLineIndex(), accuracy);
        }
        if (lines != null) {
            for (int i = lines.numLineStrings(); i-- > 0;) {
                network.explicitIntersectionsWith(lines.getLineString(i).getLineIndex(), accuracy);
            }
        }
    }
}
