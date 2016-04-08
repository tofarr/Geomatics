package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.VectList;
import org.jg.util.VectMap.VectMapProcessor;

//TODO: Do not need multi point
//TODO: Rename to LineGeom
/**
 *
 * @author tofarrell
 */
public class GeoShape implements Geom {

    // Area for this geom (may be null) 
    public final Area area;
    //ine strings which are not part of an area 
    public final LineSet lines;
    // Points which are not part of a line 
    public final PointSet points;
    // Bounds for this geo shape 
    Rect bounds;

    GeoShape(Area area, LineSet lines, PointSet points, Rect bounds) {
        this(area, lines, points);
        this.bounds = bounds;
    }

    GeoShape(Area area, LineSet lines, PointSet points) {
        this.area = area;
        this.lines = lines;
        this.points = points;
    }

    public static GeoShape valueOf(Network network, Tolerance accuracy) {
        network.clone();
        network.explicitIntersections(accuracy);
        return valueOfInternal(network, accuracy);
    }

    static GeoShape valueOfInternal(Network network, Tolerance accuracy) {
        if(network.numVects() == 0){
            return null;
        }
        VectList pointVects = new VectList();
        network.extractPoints(pointVects);
        List<VectList> linePaths = new ArrayList<>();
        network.extractHangLines(linePaths);
        Area area = Area.valueOfInternal(accuracy, network);
        if (area != null) { // validate points and lines against area
            Network lineNetwork = new Network();
            for (VectList linePath : linePaths) {
                lineNetwork.addAllLinks(linePath);
            }
            lineNetwork.addAllVertices(pointVects);
            network.removeInsideOrOutsideInternal(area, accuracy, Relate.INSIDE, new VectBuilder());
            pointVects.clear();
            lineNetwork.extractPoints(pointVects);
            linePaths.clear();
            lineNetwork.extractHangLines(linePaths);
        }
        PointSet points = null;
        if(!pointVects.isEmpty()){
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
    public PathIterator pathIterator() {
        return new PathIterator() {
            int state = nextPathIterator(-1);
            PathIterator iter;

            @Override
            public int getWindingRule() {
                return WIND_NON_ZERO;
            }

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
            public int currentSegment(float[] coords) {
                return iter.currentSegment(coords);
            }

            @Override
            public int currentSegment(double[] coords) {
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
                iter = geom.pathIterator();
                return state;
            }

        };
    }

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
    public void addTo(Network network, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        addTo(network);
    }

    @Override
    public GeoShape clone() {
        return this;
    }

    @Override
    public void toString(Appendable appendable) throws NullPointerException, GeomException {
        try {
            appendable.append("[\"GS\"");
            if (area != null) {
                appendable.append(',');
                area.toString(appendable);
            }
            if (lines != null) {
                appendable.append(',');
                lines.toString(appendable);
            }
            if (points != null) {
                appendable.append(',');
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
    public GeoShape toGeoShape(Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        return this;
    }

    @Override
    public Geom buffer(double amt, Tolerance flatness, Tolerance tolerance) throws IllegalArgumentException, NullPointerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int relate(Vect vect, Tolerance tolerance) throws NullPointerException {
        if ((points != null) && (points.relate(vect, tolerance) == Relate.TOUCH)) {
            return Relate.TOUCH;
        }
        if ((lines != null) && (lines.relate(vect, tolerance) == Relate.TOUCH)) {
            return Relate.TOUCH;
        }
        if (area != null) {
            return area.relate(vect, tolerance);
        }
        return Relate.OUTSIDE;
    }

    @Override
    public int relate(VectBuilder vect, Tolerance tolerance) throws NullPointerException {
        if ((points != null) && (points.relate(vect, tolerance) == Relate.TOUCH)) {
            return Relate.TOUCH;
        }
        if ((lines != null) && (lines.relate(vect, tolerance) == Relate.TOUCH)) {
            return Relate.TOUCH;
        }
        if (area != null) {
            return area.relate(vect, tolerance);
        }
        return Relate.OUTSIDE;
    }

    @Override
    public GeoShape union(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        return union(other.toGeoShape(flatness, accuracy), accuracy);
    }

    public GeoShape union(GeoShape other, Tolerance accuracy) {
        
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
        if((_lines != null) && (_area != null)){
            _lines = _lines.less(_area.toGeoShape(), accuracy);
        }
        
        PointSet _points;
        if (points == null) {
            _points = other.points;
        } else if (other.points == null) {
            _points = points;
        } else {
            _points = points.union(other.points, accuracy);
        }
        if((_points != null) && (_area != null)){
            _points = _points.less(_area, accuracy);
        }
        if((_points != null) && (_lines != null)){
            _points = _points.less(_lines, accuracy);
        }
        
        return new GeoShape(_area, _lines, _points, getBounds().union(other.getBounds()));
    }

    @Override
    public GeoShape intersection(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        if (getBounds().isDisjoint(other.getBounds(), accuracy)) {
            return null;
        }
        return intersection(other.toGeoShape(flatness, accuracy), accuracy);
    }

    public GeoShape intersection(GeoShape other, final Tolerance accuracy) {
        if (getBounds().isDisjoint(other.getBounds(), accuracy)) {
            return null;
        }
        
        GeoShape _area;
        if (area == null) {
            _area = (other.area == null) ? null : other.area.toGeoShape();
        } else if (other.area == null) {
            _area = area.toGeoShape();
        } else {
            _area = area.intersection(other.area, accuracy);
        }
        
        Network network = new Network();
        if (lines != null) {
            lines.addTo(network);
        }
        if(other.lines != null){
            other.lines.addTo(network);
        }
        if (points != null) {
            points.addTo(network);
        }
        if(other.points != null){
            other.points.addTo(network);
        }
        
        network.explicitIntersections(accuracy);
        LineSet _lines = LineSet.valueOfInternal(network);
        PointSet _points = PointSet.valueOf(network, accuracy);
        if((_points != null) && (_lines != null)){
            _points = _points.less(_lines, accuracy);
        }
        if(_area == null){
            return new GeoShape(null, _lines, _points);
        }
        if(_lines != null){
            _lines = _lines.less(_area, accuracy);
            if(_lines == null){
                _lines = _area.lines;
            }else if(_area.lines != null){
                _lines = _lines.union(_area.lines, accuracy);
            }
        }
        if(_points != null){
            _points = _points.less(_area, accuracy);
            if(_points == null){
                _points = _area.points;
            }else if(_area.points != null){
                _points = _points.union(_area.points, accuracy);
            }
        }
        Area _areaA = null;
        if(_area != null){
            _areaA = _area.area;
        }
        
        return (_areaA != null) || (_lines != null) || (_points != null) ? new GeoShape(_areaA, _lines, _points) : null;
    }
    
    static void removeTouchingOrInsideArea(final Network network, final Area area, final Tolerance accuracy){
        //Remove all vertices which are inside, or touch and are not linked to an external vertex
        network.map.forEach(new VectMapProcessor<VectList>() {
            @Override
            public boolean process(double x, double y, VectList links) {
                Relate relate = area.relateInternal(x, y, accuracy);
                if (relate == Relate.INSIDE) {
                    network.removeVertexInternal(x, y);
                } else if (relate == Relate.TOUCH) {
                    for (int i = links.size(); i-- > 0;) {
                        if (area.relateInternal(links.getX(i), links.getY(i), accuracy) == Relate.OUTSIDE) {
                            return true; // linked to an external vertex
                        }
                    }
                    network.removeVertexInternal(x, y);
                }
                return true;
            }

        });
    }
    
    @Override
    public GeoShape less(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        if (getBounds().isDisjoint(other.getBounds(), accuracy)) {
            return this;
        }
        return less(other.toGeoShape(flatness, accuracy), accuracy);
    }
    
    public GeoShape less(GeoShape other, final Tolerance accuracy) throws NullPointerException {
        if (getBounds().isDisjoint(other.getBounds(), accuracy)) {
            return this;
        }
        //First we get an area if there was one...
        Area _area = area;
        if ((_area != null) && (other.area != null)) {
            _area = _area.less(other.area, accuracy);
        }
        LineSet _lines = null;
        if(lines != null){
            _lines = lines.less(other, accuracy);
        }
        PointSet _points = null;
        if(points != null){
            _points = points.less(other, accuracy);
        }
        return (_area != null) || (_lines != null) || (_points != null) ? new GeoShape(_area, _lines, _points) : null;
    }
    
    void addWithRelation(Network src, final Area toTest, final Relate relate, final Tolerance accuracy, final Network result){
        src.map.forEach(new VectMapProcessor<VectList>(){
            @Override
            public boolean process(double x, double y, VectList links) {
                if(toTest.relateInternal(x, y, accuracy) == relate){
                    for(int i = links.size(); i-- > 0;){
                        result.addLinkInternal(x, y, links.getX(i), links.getY(i));
                    }
                }
                return true;
            }
        
        });
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
        if(!(obj instanceof GeoShape)){
            return false;
        }
        final GeoShape other = (GeoShape) obj;
        return Objects.equals(this.area, other.area) && 
                Objects.equals(this.lines, other.lines) &&
                Objects.equals(this.points, other.points);
    }

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
    
    public Geom simplify(){
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
    
    /*
    static Geom reduce(Area area, MultiLineString lines, PointSet mp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    */

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
            for(int i = 0; i < lines.numLineStrings(); i++){
                if(i != 0){
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
            if(area.shell == null){
                for(int i = 0; i < area.numChildren(); i++){
                    if (comma) {
                        appendable.append(',');
                    } else {
                        comma = true;
                    }
                    toPolygonWkt(area.getChild(i), appendable);
                }
            }else{
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
        if(area != null){
            network.explicitIntersectionsWith(area.getLineIndex(), accuracy);
        }
        if(lines != null){
            for(int i = lines.numLineStrings(); i-- > 0;){
                network.explicitIntersectionsWith(lines.getLineString(i).getLineIndex(), accuracy);
            }
        }
    }
}
