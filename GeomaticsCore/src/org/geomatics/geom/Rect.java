package org.geomatics.geom;

import java.beans.Transient;
import org.jayson.JaysonWriter;
import org.geomatics.geom.io.RectHandler;
import org.geomatics.util.Tolerance;
import org.geomatics.util.Transform;
import org.geomatics.util.VectList;

/**
 * Immutable 2D Rectangle. Checks are in place to insure that ordinates are never infinite or NaN,
 * or that a min value is greater than a max value.
 *
 * @author tofar_000
 */
public final class Rect implements Geom {

    public static final String CODE = "RE";
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
    public Rect add(Rect rect) throws NullPointerException {
        int relate = relate(rect, Tolerance.ZERO);
        if(!Relation.isBOutsideA(relate)){ // no part of rect is outside this
            return this;
        }else if(!Relation.isAOutsideB(relate)){
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
    public PathIter iterator() {
        return new Ring(new VectList(minX, minY,
                maxX, minY,
                maxX, maxY,
                minX, maxY,
                minX, minY), null).iterator();
    }

    @Override
    public GeoShape toGeoShape(Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        Area area = toArea();
        GeoShape ret = new GeoShape(area, null, null);
        ret.bounds = this;
        return ret;
    }

    @Override
    public void addTo(Network network, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
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
    public Geom buffer(double amt, Linearizer linearizer, Tolerance tolerance) throws IllegalArgumentException, NullPointerException {
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

        result.add(_minX, minY);
        linearizer.linearizeSegment(minX, minY, _minX, minY, minX, _minY, result);
        result.add(maxX, _minY);
        linearizer.linearizeSegment(maxX, minY, maxX, _minY, _maxX, minY, result);
        result.add(_maxX, maxY);
        linearizer.linearizeSegment(maxX, maxY, _maxX, maxY, maxX, _maxY, result);
        result.add(minX, _maxY);
        linearizer.linearizeSegment(minX, maxY, minX, _maxY, _minX, maxY, result);
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
        if(maxXRel > 0){
            return Relation.DISJOINT;
        }
        int maxYRel = tolerance.check(y - maxY);
        if(maxYRel > 0){
            return Relation.DISJOINT;
        }
        
        if((minXRel > 0) && (minYRel > 0) && (maxXRel < 0) && (maxYRel < 0)){
            return Relation.B_INSIDE_A | Relation.A_OUTSIDE_B;
        }else if((minXRel == 0) && (minYRel == 0) && (maxXRel == 0) && (maxYRel == 0)){
            return Relation.TOUCH;
        }else{
            return Relation.TOUCH | Relation.A_OUTSIDE_B;
        }
    }

    @Override
    public int relate(Geom geom, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        if(geom instanceof Rect){
            return relate((Rect)geom, accuracy);
        }
        return toRing().relate(geom, linearizer, accuracy);
    }
    
    public int relate(Rect other, Tolerance accuracy){
        return relate(minX, minY, maxX, maxY, other.minX, other.minY, other.maxX, other.maxY, accuracy);
    }  
    
    public int relate(RectBuilder other, Tolerance accuracy){
        return relate(minX, minY, maxX, maxY, other.getMinX(), other.getMinY(), other.getMaxX(), other.getMaxY(), accuracy);
    }
    
    
    public static int relate(double aMinX, double aMinY, double aMaxX, double aMaxY, double bMinX, double bMinY, double bMaxX, double bMaxY, Tolerance accuracy) {
        
        int minMaxX = accuracy.check(aMinX - bMaxX);
        if(minMaxX > 0){
            return Relation.DISJOINT;
        }
        int minMaxY = accuracy.check(aMinY - bMaxY);
        if(minMaxY > 0){
            return Relation.DISJOINT;
        }
        int maxMinX = accuracy.check(aMaxX - bMinX);
        if(maxMinX < 0){
            return Relation.DISJOINT;
        }
        int maxMinY = accuracy.check(aMaxY - bMinY);
        if(maxMinY < 0){
            return Relation.DISJOINT;
        }
        
        int minMinX = accuracy.check(aMinX - bMinX);
        int minMinY = accuracy.check(aMinY - bMinY);
        int maxMaxX = accuracy.check(aMaxX - bMaxX);
        int maxMaxY = accuracy.check(aMaxY - bMaxY);
        
        int ret = Relation.NULL;
                
        if((minMinX < 0) || (maxMaxX > 0) || (minMinY < 0) || (maxMaxY > 0)){
            ret |= Relation.A_OUTSIDE_B;
        }
        if((minMaxX < 0) && (maxMinX > 0) && (minMaxY < 0) && (maxMinY > 0)){
            ret |= Relation.B_INSIDE_A;
        }
        if((minMinX > 0) || (maxMaxX < 0) || (minMinY > 0) || (maxMaxY < 0)){
            ret |= Relation.B_OUTSIDE_A;
        }
        if((maxMinX > 0) && (minMaxX < 0) && (maxMinY > 0) && (minMaxY < 0)){
            ret |= Relation.A_INSIDE_B;
        }
        
        if((minMinX == 0) || (minMinY == 0) || (maxMaxX == 0) || (maxMaxY == 0)){
            ret |= Relation.TOUCH;
        }else if(((minMinX > 0) == (maxMaxX > 0)) || ((minMinY > 0) == (maxMaxY > 0))){
            ret |= Relation.TOUCH;
        } 

        return ret;
    }
    
    @Override
    public Geom union(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        int boundsRelate = relate(other.getBounds(), accuracy);
        if (!Relation.isBOutsideA(boundsRelate)) {
            return this;
        } else if((other instanceof Rect) && (!Relation.isAOutsideB(boundsRelate))){
            return other;
        }else{
            return toArea().union(other, linearizer, accuracy);
        }
    }
    
    @Override
    public Geom intersection(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        if(other instanceof Rect){
            return intersection((Rect)other);
        }
        int boundsRelation = relate(other.getBounds(), accuracy);
        if(boundsRelation == Relation.DISJOINT){
            return null;
        }else if(!Relation.isBOutsideA(boundsRelation)){ // no part of other is outside this
            return other;
        }else{ // long way - find intersection by area
            return toArea().intersection(other, linearizer, accuracy);
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
        int relate = relate(rect, Tolerance.ZERO);
        if(Relation.isDisjoint(relate)){
            return null;
        }else if(!Relation.isAOutsideB(relate)){
            return this;
        }else if(!Relation.isBOutsideA(relate)){
            return rect;
        } else {
            return valueOf(Math.max(minX, rect.minX),
                    Math.max(minY, rect.minY),
                    Math.min(maxX, rect.maxX),
                    Math.min(maxY, rect.maxY));
        }
    }

    @Override
    public Geom less(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        int boundsRelation = relate(other.getBounds(), accuracy);
        if(boundsRelation == Relation.DISJOINT){
            return this;
        } else if ((other instanceof Rect) && (!Relation.isAOutsideB(boundsRelation))) {
            return null; // no part of this is outside the other.
        } else {
            Area ret = toArea().less(other, linearizer, accuracy);
            return (ret == null) ? null : ret.simplify();
        }
    }

    @Override
    public Geom xor(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        return toRing().xor(other, linearizer, accuracy);
    }
    
    @Override
    public double getArea(Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        return getArea();
    }

    @Override
    public Rect clone() {
        return this;
    }
    
    /**
     * Detemine if this rectangle matches that given within the tolerance given
     * @param rect
     * @param accuracy
     * @return true if rectangles match, false otherwise
     * @throws NullPointerException if rect or accuracy was null
     */
    public boolean match(Rect rect, Tolerance accuracy) throws NullPointerException{
        return accuracy.match(minX, rect.minX)
                && accuracy.match(minY, rect.minY)
                && accuracy.match(maxX, rect.maxX)
                && accuracy.match(maxY, rect.maxY);
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

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        new RectHandler().render(this, new JaysonWriter(str));
        return str.toString();
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
