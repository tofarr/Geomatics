package org.jg.geom;

import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.TransformBuilder;
import org.jg.util.VectList;

/**
 * Class for linearizing arcs. Can be based on number of segments per quadrant, or a definitive
 * flatness value.
 *
 * @author tofarrell
 */
public class Linearizer {

    public static final Linearizer DEFAULT = new Linearizer(8, Tolerance.DEFAULT);

    private final Integer segmentsPerQuadrant;
    private final Double flatness;
    private final Tolerance precision;
    private final double flatnessSq;

    public Linearizer(Integer segmentsPerQuadrant, Tolerance precision) {
        if (segmentsPerQuadrant <= 0) {
            throw new IllegalArgumentException("Must be at least one segment per quadrant!");
        }
        if(precision == null){
            throw new NullPointerException("Precision must not be null (may be 0)");
        }
        this.segmentsPerQuadrant = segmentsPerQuadrant;
        this.flatness = null;
        this.flatnessSq = -1;
        this.precision = precision;
    }

    public Linearizer(Double flatness, Tolerance precision) {
        Vect.check(flatness, "Invalid flatness : {0}");
        if (flatness <= 0) {
            throw new IllegalArgumentException("Invalid flatness : " + flatness);
        }
        if(precision == null){
            throw new NullPointerException("Precision must not be null (may be 0)");
        }
        this.segmentsPerQuadrant = null;
        this.flatness = flatness;
        this.flatnessSq = flatness * flatness;
        this.precision = precision;
    }

    public Integer getSegmentsPerQuadrant() {
        return segmentsPerQuadrant;
    }

    public Double getFlatness() {
        return flatness;
    }

    //assumes ccw direction
    public void linearizeArc(double ox, double oy, double angleA, double angleB, double radius, VectList result){
        double ax = ox + cos(angleA) * radius;
        double ay = oy + sin(angleA) * radius;
        double angleSize = angleB - angleA;
        if (angleSize < 0) {
            angleSize += 2 * Math.PI;
        }
        if(segmentsPerQuadrant == null){
            double bx = ox + cos(angleB) * radius;
            double by = oy + sin(angleB) * radius;
            double maxDistSq = Math.pow(radius - flatness, 2);
            linearizeArcByFlatness(ox, oy, ax, ay, bx, by, maxDistSq, angleSize, radius, result);
        }else{
            linearizeByNumSegments(ox, oy, ax, ay, angleSize, result);
        }
    }
    
    public void linearizeSegment(double ox, double oy, double ax, double ay, double angleB, VectList result) throws NullPointerException, IllegalArgumentException{
        double angleA = Vect.directionInRadiansTo(ox, oy, ax, ay);
        double angleSize = angleB - angleA;
        if (angleSize < 0) {
            angleSize += 2 * Math.PI;
        }
        if(segmentsPerQuadrant == null){
            double radius = Math.sqrt(Vect.distSq(ax, ay, ox, oy));
            double bx = ox + cos(angleB) * radius;
            double by = oy + sin(angleB) * radius;
            double maxDistSq = Math.pow(radius - flatness, 2);
            linearizeArcByFlatness(ox, oy, ax, ay, bx, by, maxDistSq, angleSize, radius, result);
        }else{
            linearizeByNumSegments(ox, oy, ax, ay, angleSize, result);
        }
    }
    
    public void linearizeSegment(double ox, double oy, double ax, double ay, double bx, double by, VectList result){
        double angleA = Vect.directionInRadiansTo(ox, oy, ax, ay);
        double angleB = Vect.directionInRadiansTo(ox, oy, bx, by);
        double angleSize = angleB - angleA;
        if (angleSize < 0) {
            angleSize += 2 * Math.PI;
        }
        if(segmentsPerQuadrant == null){
            double radius = Math.sqrt(Vect.distSq(ax, ay, ox, oy));
            double maxDistSq = Math.pow(radius - flatness, 2);
            linearizeArcByFlatness(ox, oy, ax, ay, bx, by, maxDistSq, angleSize, radius, result);
        }else{
            linearizeByNumSegments(ox, oy, ax, ay, angleSize, result);
        }
    }
    
    public void bezierQuad(double ax, double ay, double bx, double by, double cx, double cy, VectList result){
        if(segmentsPerQuadrant == null){
            quadByFlatness(ax, ay, bx, by, cx, cy, result);
        }else{
            //split into segments per quadrant
            VectBuilder workingVect = new VectBuilder();
            double segmentSize = 1.0/segmentsPerQuadrant;
            for(int i = 1 ; i < segmentsPerQuadrant; i++){
                double t = i * segmentSize;
                quad(ax, ay, bx, by, cx, cy, t, workingVect);
                result.add(workingVect);
            }
        }
    }
    
    public void bezierCubic(double ax, double ay, double bx, double by, double cx, double cy, double dx, double dy, VectList result){
        if(segmentsPerQuadrant == null){
            cubicByFlatness(ax, ay, bx, by, cx, cy, dx, dy, result);
        }else{
            //split into segments per quadrant
            VectBuilder workingVect = new VectBuilder();
            double segmentSize = 1.0/segmentsPerQuadrant;
            for(int i = 1 ; i < segmentsPerQuadrant; i++){
                double t = i * segmentSize;
                cubic(ax, ay, bx, by, cx, cy, dx, dy, t, workingVect);
                result.add(workingVect);
            }
        }
    }
    
    private void cubicByFlatness(double ax, double ay, double bx, double by, double cx, double cy, double dx, double dy, VectList result){
        //if b and c are more than flatness distance from ad, subdivide, otherwise add d to result
        if((Line.distLineVectSq(ax, ay, dx, dy, bx, by) <= flatnessSq) && (Line.distLineVectSq(ax, ay, dx, dy, cx, cy) <= flatnessSq)){
            result.add(dx, dy);
            return;
        }
        
        //subdivide
        double abx = mid(ax, bx);
        double aby = mid(ay, by);
        double bcx = mid(bx, cx);
        double bcy = mid(by, cy);
        double cdx = mid(cx, dx);
        double cdy = mid(cy, dy);
        
        double abcx = mid(abx, bcx);
        double abcy = mid(aby, bcy);
        double bcdx = mid(bcx, cdx);
        double bcdy = mid(bcy, cdy);
        
        double abcdx = mid(abcx, bcdx);
        double abcdy = mid(abcy, bcdy);
        
        cubicByFlatness(ax, ay, abx, aby, abcx, abcy, abcdx, abcdy, result);
        cubicByFlatness(abcdx, abcdy, bcdx, bcdy, cdx, cdy, dx, dy, result);
        
    }
    
    private void quadByFlatness(double ax, double ay, double bx, double by, double cx, double cy, VectList result){
        //if b and c are more than flatness distance from ad, subdivide, otherwise add d to result
        if(Line.distLineVectSq(ax, ay, cx, cy, bx, by) <= flatnessSq){
            result.add(cx, cy);
            return;
        }
        
        //subdivide
        double abx = mid(ax, bx);
        double aby = mid(ay, by);
        double bcx = mid(bx, cx);
        double bcy = mid(by, cy);
        
        double abcx = mid(abx, bcx);
        double abcy = mid(aby, bcy);
        
        quadByFlatness(ax, ay, abx, aby, abcx, abcy, result);
        quadByFlatness(abcx, abcy, bcx, bcy, cx, cy, result);
        
    }
    
    static void cubic(double ax, double ay, double bx, double by, double cx, double cy, double dx, double dy, double t, VectBuilder result){
        quad(progress(ax, bx, t), progress(ay, by, t), progress(bx, cx, t), progress(by, cy, t), progress(cx, dx, t), progress(cy, dy, t), t, result);   
    }
    
    static void quad(double ax, double ay, double bx, double by, double cx, double cy, double t, VectBuilder result){
        linear(progress(ax, bx, t), progress(ay, by, t), progress(bx, cx, t), progress(by, cy, t), t, result);
    }
    
    static void linear(double ax, double ay, double bx, double by, double t, VectBuilder result){
        result.set(progress(ax, bx, t), progress(ay, by, t));
    }
    
    static double mid(double a, double b){
        return (a + b) / 2;
    }
    
    static double progress(double a, double b, double t){
        return (b - a) * t + a;
    }
    
    private void linearizeByNumSegments(double ox, double oy, double ax, double ay, double angleSize, VectList result) throws NullPointerException {
        
        int numSegments = (int)Math.round(Math.abs(segmentsPerQuadrant * angleSize * 2 / Math.PI));
        double segmentSize = angleSize / numSegments;
        Transform transform = new TransformBuilder().rotateRadiansAround(segmentSize, ox, oy).build();
        VectBuilder vect = new VectBuilder();
        vect.set(ax, ay);
        double angle = 0;
        for(int i = 0; i < numSegments; i++){
            angle += segmentSize;
            //Rounding errors here are onconvenient, so we check if the angle is within a tolerance of
            //a 90 degree angle and set values directly
            if (precision.match(angle, Math.PI / 2)) { // 90 degree shift
                vect.set(ox - (ay - oy), oy + (ax - ox));
            } else if (precision.match(angle, Math.PI)) { // 180 degree shift
                vect.set(ox + ox - ax, oy + oy - ay);
            } else if (precision.match(angle, Math.PI * 3 / 2)) { // 270 degree shift
                vect.set(ox + (ay - oy), oy - (ax - ox));
            } else if (precision.match(angle, 2 * Math.PI)){ // 360 degree shift
                vect.set(ax, ay);
            }else{ // normal transform
                transform.transform(vect, vect);
            }
            result.add(vect);
        }
    }
    
    private void linearizeArcByFlatness(double ox, double oy, double ax, double ay, double bx, double by,
            double maxDistSq, double angleSize, double radius, VectList result){
        if(flatness >= radius){
            result.add(bx, by);
        }else{
            linearizeByFlatness(ox, oy, ax, ay, bx, by, maxDistSq, angleSize, radius, result);
        }
    }
    
    private void linearizeByFlatness(double ox, double oy, double ax, double ay, double bx, double by,
            double maxDistSq, double angleSize, double radius, VectList result){

        double mx = (ax + bx) / 2; //get mid point between a and b
        double my = (ay + by) / 2;

        boolean tooLarge = Math.abs(angleSize) >= Math.PI;
        if (tooLarge) { // If angle is greater than 180 degrees, invert vector direction.
            mx = ox + (ox - mx);
            my = oy + (oy - my);
        }

        double distSq = Vect.distSq(ox, oy, mx, my);
        if ((!tooLarge) && (distSq >= maxDistSq)) { // If the value is less than flatness, simply add
            result.add(bx, by);
        } else {
            double nx, ny;
            if (distSq == 0) { // project normal 
                double dx = mx - ax;
                double dy = my - ay;
                nx = mx + dy; // normals are (-dy,dx) and (dy,-dx)
                ny = my - dx; //TODO : is this on the right?
            } else {
                double dist = Math.sqrt(distSq);
                nx = (mx - ox) * radius / dist + ox; //calculate a new mid point
                ny = (my - oy) * radius / dist + oy;
            }
            angleSize /= 2; // angle size is halved

            linearizeByFlatness(ox, oy, ax, ay, nx, ny, maxDistSq, angleSize, radius, result);
            linearizeByFlatness(ox, oy, nx, ny, bx, by, maxDistSq, angleSize, radius, result);
        }
    }
      
    //Get cosine of angle. Math.cos returns numbers very close to but not quite zero for PI/2 and PI
    //Thie attempts to correct this.
    private static double cos(double angle){
        if((angle == Math.PI / 2) || (angle == Math.PI * 3 / 2)){
            return 0;
        }else{
            return Math.cos(angle);
        }
    }
    
    //Get sin of angle. Math.sin returns numbers very close to but not quite zero for PI and 2PI
    //Thie attempts to correct this.
    private static double sin(double angle){
        if((angle == Math.PI) || (angle == Math.PI * 2)){
            return 0;
        }else{
            return Math.sin(angle);
        }
    }
}
