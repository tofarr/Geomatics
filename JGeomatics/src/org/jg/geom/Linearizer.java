package org.jg.geom;

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

    public static final Linearizer DEFAULT = new Linearizer(8);

    private final Integer segmentsPerQuadrant;
    private final Double flatness;

    public Linearizer(Integer segmentsPerQuadrant) {
        if (segmentsPerQuadrant <= 0) {
            throw new IllegalArgumentException("Must be at least one segment per quadrant!");
        }
        this.segmentsPerQuadrant = segmentsPerQuadrant;
        this.flatness = null;
    }

    public Linearizer(Double flatness) {
        Vect.check(flatness, "Invalid flatness : {0}");
        if (flatness <= 0) {
            throw new IllegalArgumentException("Invalid flatness : " + flatness);
        }
        this.segmentsPerQuadrant = null;
        this.flatness = flatness;
    }

    public Integer getSegmentsPerQuadrant() {
        return segmentsPerQuadrant;
    }

    public Double getFlatness() {
        return flatness;
    }

    //assumes ccw direction
    public void linearizeArc(double ox, double oy, double angleA, double angleB, double radius, VectList result){
        double ax = ox + Math.cos(angleA) * radius;
        double ay = oy + Math.sin(angleA) * radius;
        double angleSize = angleB - angleA;
        if (angleSize < 0) {
            angleSize += 2 * Math.PI;
        }
        if(segmentsPerQuadrant == null){
            double bx = ox + Math.cos(angleB) * radius;
            double by = oy + Math.sin(angleB) * radius;
            double maxDistSq = Math.pow(radius - flatness, 2);
            linearizeByFlatness(ox, oy, ax, ay, bx, by, maxDistSq, angleSize, radius, result);
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
            double bx = ox + Math.cos(angleB) * radius;
            double by = oy + Math.sin(angleB) * radius;
            double maxDistSq = Math.pow(radius - flatness, 2);
            linearizeByFlatness(ox, oy, ax, ay, bx, by, maxDistSq, angleSize, radius, result);
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
            linearizeByFlatness(ox, oy, ax, ay, bx, by, maxDistSq, angleSize, radius, result);
        }else{
            linearizeByNumSegments(ox, oy, ax, ay, angleSize, result);
        }
    }
    
    private void linearizeByNumSegments(double ox, double oy, double ax, double ay, double angleSize, VectList result) throws NullPointerException {
        
        int numSegments = (int)Math.round(Math.abs(segmentsPerQuadrant * angleSize * 2 / Math.PI));
        double segmentSize = angleSize / numSegments;
        Transform transform = new TransformBuilder().rotateRadiansAround(segmentSize, ox, oy).build();
        VectBuilder vect = new VectBuilder();
        vect.set(ax, ay);
        for(int i = 0; i < numSegments; i++){
            transform.transform(vect, vect);
            result.add(vect);
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
}
