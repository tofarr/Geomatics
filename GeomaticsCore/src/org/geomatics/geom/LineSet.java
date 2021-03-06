package org.geomatics.geom;

import java.io.IOException;
import java.util.Arrays;
import org.geomatics.geom.Network.LinkProcessor;
import org.geomatics.geom.io.GeomIOException;
import org.geomatics.geom.io.GeomJaysonWriter;
import org.geomatics.geom.io.LineSetHandler;
import org.geomatics.util.Tolerance;
import org.geomatics.util.Transform;
import org.geomatics.util.VectList;
import org.geomatics.util.VectMap.VectMapProcessor;

/**
 * Immutable set of 2D lines. Checks are in place to insure that no ordinates are NaN or Infinite,
 * and that no line segment crosses or lies on another
 *
 * @author tofar
 */
public final class LineSet implements Geom {

    public static final String CODE = "LT";
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
    public PathIter iterator() {
        PathIter iter = new PathIter() {
            PathIter iter = lineStrings[0].iterator();
            int index = 0;

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
                            iter = lineStrings[index].iterator();
                        } else {
                            iter = null;
                        }
                    }
                }
            }

            @Override
            public PathSegType currentSegment(double[] coords) {
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
        new LineSetHandler().render(this, new GeomJaysonWriter(str));
        return str.toString();
    }
    
    public String toWkt() {
        StringBuilder str = new StringBuilder();
        toWkt(str);
        return str.toString();
    }
    
    public void toWkt(Appendable appendable) throws GeomIOException {
        if(lineStrings.length == 1){
            lineStrings[0].toWkt(appendable);
            return;
        }
        try {
            appendable.append("MULTILINESTRING(");
            for(int i = 0; i < lineStrings.length; i++){
                if(i != 0){
                    appendable.append(',');
                }
                lineStrings[i].vects.toString(appendable, '(', ')', ' ');
            }
            appendable.append(')');
        } catch (IOException ex) {
            throw new GeomIOException("Error writing", ex);
        }
    }

    @Override
    public GeoShape toGeoShape(Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
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
    public void addTo(Network network, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
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
    public Geom buffer(double amt, Linearizer linearizer, Tolerance accuracy) throws IllegalArgumentException, NullPointerException {
        if (amt < 0) {
            return null;
        } else if (amt == 0) {
            return this;
        }
        Geom ret = null;
        for (LineString lineString : lineStrings) {
            Geom buffer = lineString.buffer(amt, linearizer, accuracy);
            ret = (ret == null) ? buffer : ret.union(buffer, linearizer, accuracy);
        }
        return ret;
    }

    @Override
    public int relate(Vect vect, Tolerance accuracy) throws NullPointerException {
        return relateInternal(vect.x, vect.y, accuracy);
    }

    @Override
    public int relate(VectBuilder vect, Tolerance accuracy) throws NullPointerException {
        return relateInternal(vect.getX(), vect.getY(), accuracy);
    }  
    
    @Override
    public int relate(Geom geom, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        int ret = NetworkRelationProcessor.relate(this, geom, linearizer, accuracy);
        if(!Relation.isBOutsideA(ret) && (geom.getArea(linearizer, accuracy) != 0)){
            ret |= Relation.B_OUTSIDE_A;
        }
        return ret;
    }

    int relateInternal(double x, double y, Tolerance accuracy) {
        if (Relation.isDisjoint(getBounds().relateInternal(x, y, accuracy))) {
            return Relation.DISJOINT;
        }
        for (LineString lineString : lineStrings) {
            if (Relation.isTouch(lineString.relateInternal(x, y, accuracy))) {
                return Relation.TOUCH | Relation.A_OUTSIDE_B;
            }
        }
        return Relation.DISJOINT;
    }

    @Override
    public Geom union(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        if (other instanceof LineString) {
            return union((LineString)other, accuracy).simplify();
        } else if (other instanceof LineSet) {
            return union((LineSet) other, accuracy).simplify();
        } else {
            return toGeoShape().union(other, linearizer, accuracy);
        }
    }
    
    /**
     * Get the union of this line string and that given
     *
     * @param other
     * @param accuracy
     * @return a line set
     * @throws NullPointerException if other or accuracy was null
     */
    public LineSet union(LineString other, Tolerance accuracy){
        return union(other.toLineSet(), accuracy);
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
        if (Relation.isDisjoint(getBounds().relate(other.getBounds(), accuracy))) {
            LineString[] lines = new LineString[numLineStrings() + other.numLineStrings()];
            System.arraycopy(lineStrings, 0, lines, 0, lineStrings.length);
            System.arraycopy(other.lineStrings, 0, lines, lineStrings.length, other.lineStrings.length);
            Arrays.sort(lines, COMPARATOR);
            return new LineSet(lines);
        }
        Network network = Network.valueOf(accuracy, Linearizer.DEFAULT, this, other);
        LineString[] ret = LineString.parseAllInternal(network);
        return new LineSet(ret);
    }

    @Override
    public Geom intersection(final Geom other, Linearizer linearizer, final Tolerance accuracy) throws NullPointerException {
        if (Relation.isDisjoint(other.getBounds().relate(getBounds(), accuracy))) { // quick way - disjoint
            return null;
        }
        final Network network = Network.valueOf(accuracy, linearizer, this, other);
        final Network intersection = new Network();
        network.map.forEach(new VectMapProcessor<VectList>(){
            final VectBuilder workingVect = new VectBuilder();
            @Override
            public boolean process(double x, double y, VectList links) {
                workingVect.set(x, y);
                if(Relation.isBOutsideA(LineSet.this.relate(workingVect, accuracy))
                    || Relation.isBOutsideA(other.relate(workingVect, accuracy))){
                    return true;
                }
                intersection.addVertex(workingVect);

                //Process links
                for(int i = links.size(); i-- > 0;){
                    double bx = links.getX(i);
                    double by = links.getY(i);
                    if(Vect.compare(x, y, bx, by) < 0){ // prevent processing twice
                        workingVect.set((x + bx) / 2, (y + by) / 2);
                        if(!(Relation.isBOutsideA(LineSet.this.relate(workingVect, accuracy))
                            || Relation.isBOutsideA(other.relate(workingVect, accuracy)))){
                            intersection.addLinkInternal(x, y, bx, by);
                        }
                    }
                }

                return true;
            }
        });

        LineSet lines = LineSet.valueOfInternal(intersection);
        PointSet points = PointSet.valueOfInternal(intersection);
        if((lines == null) && (points == null)){
            return null;
        }
        GeoShape geoShape = new GeoShape(null, lines, points);
        return geoShape.simplify(); //Intersection may be null,Vect,PointSet,Line,LineString,LineSet,GeoShape
    }

    @Override
    public LineSet less(final Geom other, Linearizer linearizer, final Tolerance accuracy) throws NullPointerException {
        if (Relation.isDisjoint(other.getBounds().relate(getBounds(), accuracy))) { // quick way - disjoint
            return this;
        }
        final Network network = Network.valueOf(accuracy, linearizer, this, other);
        final Network less = new Network();
        network.forEachLink(new LinkProcessor(){
            final VectBuilder workingVect = new VectBuilder();
            @Override
            public boolean process(double ax, double ay, double bx, double by) {
                workingVect.set((ax + bx) / 2, (ay + by) / 2);
                if(Relation.isTouch(LineSet.this.relate(workingVect, accuracy)) &&
                        Relation.isBOutsideA(other.relate(workingVect, accuracy))){ // any links not in original should be added
                    less.addLinkInternal(ax, ay, bx, by);
                }
                return true;
            }
        });

        return LineSet.valueOfInternal(less); //Intersection may be null,Line,LineString,LineSet
    }
    
    
    @Override
    public Geom xor(final Geom other, Linearizer linearizer, final Tolerance accuracy) throws NullPointerException {
        if (other instanceof LineString) {
            LineSet ret = xor((LineString)other, accuracy);
            return (ret == null) ? null : ret.simplify();
        } else if (other instanceof LineSet) {
            LineSet ret = xor((LineSet) other, accuracy);
            return (ret == null) ? null : ret.simplify();
        } else {
            GeoShape ret = toGeoShape().xor(other, linearizer, accuracy);
            return (ret == null) ? null : ret.simplify();
        }
    }
    
    /**
     * xor this lineset and the linestring given
     * @param other
     * @param accuracy
     * @return
     */
    public LineSet xor(LineString other, Tolerance accuracy) throws NullPointerException {
        return xor(other.toLineSet(), accuracy);
    }
    
    /**
     * xor this lineset and the linestring given
     * @param other
     * @param accuracy
     * @return
     */
    public LineSet xor(final LineSet other, final Tolerance accuracy) throws NullPointerException {
        final Network network = Network.valueOf(accuracy, Linearizer.DEFAULT, this, other);
        final VectBuilder workingVect = new VectBuilder();
        network.forEachLink(new LinkProcessor(){
            @Override
            public boolean process(double ax, double ay, double bx, double by){
                workingVect.set((ax+bx)/2, (ay+by)/2);
                if(Relation.isTouch(LineSet.this.relate(workingVect, accuracy))
                        && Relation.isTouch(other.relate(workingVect, accuracy))){
                    network.removeLinkInternal(ax, ay, bx, by); // Anything not in original should not be in new...
                }
                
                return true;
            }
        });
        return LineSet.valueOfInternal(network); //Intersection may be null,Line,LineString,LineSet
    }
    
    @Override
    public double getArea(Linearizer linearizer, Tolerance accuracy){
        return 0;
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
