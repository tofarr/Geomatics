package org.jg.geom;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.beans.Transient;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.VectList;

/**
 * Immutable 2D Rectangle. Checks are in place to insure that ordinates are never infinite or NaN,
 * or that a min value is greater than a max value.
 *
 * @author tofar_000
 */
public class Rect implements Geom {

    /**
     * min X value
     */
    public final double minX;
    /**
     * min Y value
     */
    public final double minY;
    /**
     * max X value
     */
    public final double maxX;
    /**
     * max Y value
     */
    public final double maxY;

    Rect(double minX, double minY, double maxX, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    /**
     * Get a Rect based on the values given. Swap ordinates of max < min
     *
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     * @return a Rect
     * @throws IllegalArgumentException if an ordinate was infinite or NaN
     */
    public static Rect valueOf(double minX, double minY, double maxX, double maxY) throws IllegalArgumentException {
        check(minX, minY, maxX, maxY);
        if (minX > maxX) {
            double tmp = minX;
            minX = maxX;
            maxX = tmp;
        }
        if (minY > maxY) {
            double tmp = minY;
            minY = maxY;
            maxY = tmp;
        }
        return new Rect(minX, minY, maxX, maxY);
    }

    /**
     * Get min X value
     *
     * @return
     */
    public double getMinX() {
        return minX;
    }

    /**
     * Get min Y value
     *
     * @return
     */
    public double getMinY() {
        return minY;
    }

    /**
     * Get max X value
     *
     * @return
     */
    public double getMaxX() {
        return maxX;
    }

    /**
     * Get max Y value
     *
     * @return
     */
    public double getMaxY() {
        return maxY;
    }

    /**
     * Get width. Note: May be invalid if rect is invalid
     *
     * @return
     */
    @Transient
    public double getWidth() {
        return maxX - minX;
    }

    /**
     * Get height Note: May be invalid if rect is invalid
     *
     * @return
     */
    @Transient
    public double getHeight() {
        return maxY - minY;
    }

    /**
     * Get area Note: May be invalid if rect is invalid
     *
     * @return
     */
    @Transient
    public double getArea() {
        return getWidth() * getHeight();
    }

    /**
     * Get centroid x
     *
     * @return
     */
    @Transient
    public double getCx() {
        return (minX + maxX) / 2;
    }

    /**
     * Get centroid y
     *
     * @return
     */
    @Transient
    public double getCy() {
        return (minY + maxY) / 2;
    }

    /**
     * Get the centroid of this rect
     *
     * @return
     */
    @Transient
    public Vect getCentroid() {
        return new Vect(getCx(), getCy());
    }

    /**
     * Get the intersection between this and the rect given
     *
     * @param rect
     * @return result
     * @throws NullPointerException if rect was null
     */
    public Rect union(Rect rect) throws NullPointerException {
        int relate = relate(rect, Tolerance.ZERO);
        if(!Relation.isOutside(relate)){ // no part of rect is outside this
            return this;
        }else if(!Relation.isOutsideOther(relate)){
            return rect;
        } else {
            return new Rect(Math.min(minX, rect.minX),
                    Math.min(minY, rect.minY),
                    Math.max(maxX, rect.maxX),
                    Math.max(maxY, rect.maxY));
        }
    }

    /**
     * Get a Rect based on this buffered by the amount given
     *
     * @param amt
     * @return buffered Rect
     * @throws IllegalArgumentException if amt was infinite or NaN
     */
    public Rect buffer(double amt) throws IllegalArgumentException {
        Vect.check(amt, "Invalid buffer amount {0}");
        if (amt == 0) {
            return this;
        }
        if (amt < 0) {
            if (((minX - amt) > (maxX + amt)) || ((minY - amt) > (maxY + amt))) {
                return null;
            }
        }
        return new Rect(minX - amt,
                minY - amt,
                maxX + amt,
                maxY + amt);
    }

    @Override
    @Transient
    public Rect getBounds() {
        return this;
    }

    @Override
    public Rect transform(Transform transform) throws NullPointerException {
        RectBuilder rect = new RectBuilder();
        VectBuilder vect = new VectBuilder();
        vect.set(minX, minY);
        transform.transform(vect, vect);
        rect.add(vect);
        vect.set(minX, maxY);
        transform.transform(vect, vect);
        rect.add(vect);
        vect.set(maxX, minY);
        transform.transform(vect, vect);
        rect.add(vect);
        vect.set(maxX, maxY);
        transform.transform(vect, vect);
        rect.add(vect);
        return rect.build();
    }

    @Override
    public PathIterator pathIterator() {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(minX, minY);
        path.lineTo(minX, maxY);
        path.lineTo(maxX, maxY);
        path.lineTo(maxX, minY);
        path.closePath();
        return path.getPathIterator(null);
    }

    @Override
    public GeoShape toGeoShape(Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        Area area = toArea();
        GeoShape ret = new GeoShape(area, null, null);
        ret.bounds = this;
        return ret;
    }

    @Override
    public void addTo(Network network, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        network.addLink(minX, minY, minX, maxY);
        network.addLink(minX, minY, maxX, minY);
        network.addLink(minX, maxY, maxX, maxY);
        network.addLink(maxX, minY, maxX, maxY);
    }

    /**
     * Create a new area based on this rect
     *
     * @return
     */
    public Area toArea() {
        return new Area(toRing());
    }

    /**
     * Create a new ring based on this rect
     *
     * @return
     */
    public Ring toRing() {
        VectList vects = new VectList(5);
        vects.add(minX, minY);
        vects.add(maxX, minY);
        vects.add(maxX, maxY);
        vects.add(minX, maxY);
        vects.add(minX, minY);
        double length = (getWidth() + getHeight()) * 2;
        Ring ring = new Ring(vects, null, getArea(), length, getCentroid(), true);
        return ring;
    }

    @Override
    public Geom buffer(double amt, Tolerance flatness, Tolerance tolerance) throws IllegalArgumentException, NullPointerException {
        if (amt == 0) {
            return this;
        } else if (amt < 0) {
            return buffer(amt);
        }
        VectList result = new VectList();
        VectBuilder vect = new VectBuilder();

        double _minX = minX - amt;
        double _minY = minY - amt;
        double _maxX = maxX + amt;
        double _maxY = maxY + amt;

        Vect.linearizeArc(minX, minY, _minX, minY, minX, _minY, amt, flatness.getTolerance(), result);
        Vect.linearizeArc(maxX, minY, maxX, _minY, _maxX, minY, amt, flatness.getTolerance(), result);
        Vect.linearizeArc(maxX, maxY, _maxX, maxY, maxX, _maxY, amt, flatness.getTolerance(), result);
        Vect.linearizeArc(minX, maxY, minX, _maxY, _minX, maxY, amt, flatness.getTolerance(), result);
        result.add(_minX, minY);

        return new Area(new Ring(result, null, null, null, getCentroid(), true));
    }

    @Override
    public int relate(Vect vect, Tolerance tolerance) throws NullPointerException {
        return relateInternal(vect.x, vect.y, tolerance);
    }

    @Override
    public int relate(VectBuilder vect, Tolerance tolerance) throws NullPointerException {
        return relateInternal(vect.getX(), vect.getY(), tolerance);
    }

    int relateInternal(double x, double y, Tolerance tolerance) throws NullPointerException {
        return relate(x, y, minX, minY, maxX, maxY, tolerance);
    }

    public static int relate(double x, double y, double minX, double minY, double maxX, double maxY, Tolerance tolerance) throws NullPointerException {
        
        int minXRel = tolerance.check(x - minX);
        if(minXRel < 0){
            return Relation.DISJOINT;
        }
        int minYRel = tolerance.check(y - minY);
        if(minYRel < 0){
            return Relation.DISJOINT;
        }
        int maxXRel = tolerance.check(x - maxX);
        if(maxXRel < 0){
            return Relation.DISJOINT;
        }
        int maxYRel = tolerance.check(y - maxY);
        if(maxYRel < 0){
            return Relation.DISJOINT;
        }
        
        if((minXRel > 0) && (minYRel > 0) && (maxXRel < 0) && (maxYRel < 0)){
            return Relation.INSIDE | Relation.OUTSIDE_OTHER;
        }else if((minXRel == 0) && (minYRel == 0) && (maxXRel == 0) && (maxYRel == 0)){
            return Relation.TOUCH;
        }else{
            return Relation.TOUCH | Relation.OUTSIDE_OTHER;
        }
    }

    @Override
    public int relate(Geom geom, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        if(geom instanceof Rect){
            return relate((Rect)geom, accuracy);
        }
        return GeomRelationProcessor.relate(this, geom, flatness, accuracy);
    }
    
    public int relate(Rect other, Tolerance accuracy){
        return relate(minX, minY, maxX, maxY, other.minX, other.minY, other.maxX, other.maxY, accuracy);
    }  
    
    public int relate(RectBuilder other, Tolerance accuracy){
        return relate(minX, minY, maxX, maxY, other.getMinX(), other.getMinY(), other.getMaxX(), other.getMaxY(), accuracy);
    }
    
    
    public static int relate(double aMinX, double aMinY, double aMaxX, double aMaxY, double bMinX, double bMinY, double bMaxX, double bMaxY, Tolerance accuracy) {
        
        if( accuracy.check(aMinX - bMaxX) > 0){
            return Relation.DISJOINT;
        }
        if(accuracy.check(aMinY - bMaxY) > 0){
            return Relation.DISJOINT;
        }
        if(accuracy.check(bMinX - aMaxX) > 0){
            return Relation.DISJOINT;
        }
        if(accuracy.check(bMinY - aMaxY) > 0){
            return Relation.DISJOINT;
        }

        int minMinX = accuracy.check(aMinX - bMinX);
        int minMinY = accuracy.check(aMinY - bMinY);
        int maxMaxX = accuracy.check(aMaxX - bMaxX);
        int maxMaxY = accuracy.check(aMaxY - bMaxY);
        
        int ret = Relation.NULL;
        if((minMinX < 0) || (minMinY < 0) || (maxMaxX > 0) || (maxMaxY > 0)){
            ret |= Relation.INSIDE | Relation.OUTSIDE_OTHER;
        }
        if((minMinX > 0) || (minMinY > 0) || (maxMaxX < 0) || (maxMaxY < 0)){
            ret |= Relation.OUTSIDE | Relation.INSIDE_OTHER;
        }
        if((minMinX == 0) || (minMinY == 0) || (maxMaxX == 0) || (maxMaxY == 0)){
            ret |= Relation.TOUCH;
        }else if(((minMinX > 0) == (maxMaxX > 0)) || ((minMinY > 0) == (maxMaxY > 0))){
            ret |= Relation.TOUCH;
        }
        return ret;
    }
    
    @Override
    public Geom union(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        if(other instanceof Rect){
            return union((Rect)other);
        }else if (!Relation.isOutside(relate(other.getBounds(), accuracy))) {
            return this;
        } else {
            return toArea().union(other, flatness, accuracy);
        }
    }

    @Override
    public Geom intersection(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        if(other instanceof Rect){
            return intersection((Rect)other);
        }
        int boundsRelation = relate(other.getBounds(), accuracy);
        if(boundsRelation == Relation.DISJOINT){
            return null;
        }else if(!Relation.isOutside(boundsRelation)){ // no part of other is outside this
            return other;
        }else{ // long way - find intersection by area
            return toArea().intersection(other, flatness, accuracy);
        }
    }
    
    /**
     * Get the intersection between this and the rect given
     *
     * @param rect
     * @return result
     * @throws NullPointerException if rect was null
     */
    public Rect intersection(Rect rect) throws NullPointerException {
        if (relate(rect, Tolerance.ZERO) == Relation.DISJOINT) {
            return null;
        } else {
            return valueOf(Math.max(minX, rect.minX),
                    Math.max(minY, rect.minY),
                    Math.min(maxX, rect.maxX),
                    Math.min(maxY, rect.maxY));
        }
    }

    @Override
    public Geom less(Geom other, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        int boundsRelation = relate(other.getBounds(), accuracy);
        if(boundsRelation == Relation.DISJOINT){
            return this;
        } else if ((other instanceof Rect) && (!Relation.isOutsideOther(boundsRelation))) {
            return null; // no part of this is outside the other.
        } else {
            return toArea().less(other, flatness, accuracy);
        }
    }

    @Override
    public Rect clone() {
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Vect.hash(minX);
        hash = 43 * hash + Vect.hash(minY);
        hash = 43 * hash + Vect.hash(maxX);
        hash = 43 * hash + Vect.hash(maxY);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Rect) {
            Rect rect = (Rect) obj;
            return (minX == rect.minX)
                    && (minY == rect.minY)
                    && (maxX == rect.maxX)
                    && (maxY == rect.maxY);
        } else {
            return false;
        }
    }

    /**
     * Write this rect to the output given
     *
     * @param out
     * @throws IOException
     */
    public void write(DataOutput out) throws IOException {
        try {
            out.writeDouble(minX);
            out.writeDouble(minY);
            out.writeDouble(maxX);
            out.writeDouble(maxY);
        } catch (IOException ex) {
            throw new GeomException("Error writing", ex);
        }
    }

    /**
     * Read a Rectangle from the input given
     *
     * @param in
     * @return a Rectangle
     * @throws GeomException
     */
    public static Rect read(DataInput in) throws GeomException {
        try {
            return valueOf(in.readDouble(), in.readDouble(), in.readDouble(), in.readDouble());
        } catch (IOException ex) {
            throw new GeomException("Error reading", ex);
        }
    }

    @Override
    public String toString() {
        return toString(minX, minY, maxX, maxY);
    }

    @Override
    public void toString(Appendable appendable) throws NullPointerException, GeomException {
        toString(minX, minY, maxX, maxY, appendable);
    }

    static String toString(double minX, double minY, double maxX, double maxY) {
        StringBuilder str = new StringBuilder();
        toString(minX, minY, maxX, maxY, str);
        return str.toString();
    }

    static void toString(double minX, double minY, double maxX, double maxY, Appendable appendable) {
        try {
            appendable.append("[\"RE\",")
                    .append(Vect.ordToStr(minX)).append(',')
                    .append(Vect.ordToStr(minY)).append(',')
                    .append(Vect.ordToStr(maxX)).append(',')
                    .append(Vect.ordToStr(maxY)).append(']');
        } catch (IOException ex) {
            throw new GeomException("Error writing", ex);
        }
    }

    /**
     * Write the rectangle given to the appendable given
     *
     * @param rect
     * @param appendable
     * @throws GeomException if there was an IO error
     */
    public static void toString(Rect rect, Appendable appendable) throws GeomException {
        try {
            appendable.append('[')
                    .append(Vect.ordToStr(rect.minX)).append(',')
                    .append(Vect.ordToStr(rect.minY)).append(',')
                    .append(Vect.ordToStr(rect.maxX)).append(',')
                    .append(Vect.ordToStr(rect.maxY)).append(']');
        } catch (IOException ex) {
            throw new GeomException("Error writing", ex);
        }
    }

    /**
     * Check the rectangle defined by the ordinates given.
     *
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     * @throws IllegalArgumentException if an ordinate was infinite or NaN
     */
    public static void check(double minX, double minY, double maxX, double maxY) throws IllegalArgumentException {
        Vect.check(minX, "Invalid minX : {0}");
        Vect.check(minY, "Invalid minY : {0}");
        Vect.check(maxX, "Invalid maxX : {0}");
        Vect.check(maxY, "Invalid maxY : {0}");
    }
}
