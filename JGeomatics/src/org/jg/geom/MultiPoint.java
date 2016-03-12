package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.Serializable;
import org.jg.util.SpatialNode;
import org.jg.util.SpatialNode.NodeProcessor;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.VectList;

/**
 *
 * @author tofar_000
 */
public class MultiPoint implements Serializable, Geom {

    final VectList vects;

    MultiPoint(VectList vects) throws NullPointerException {
        vects.sort();
        this.vects = vects;
    }

    static Geom valueOf(VectList vects){
        switch(vects.size()){
            case 0:
                return null;
            case 1:
                return vects.getVect(0);
            default:
                vects.sort();
                return new MultiPoint(vects);
        }
    }
    
    public int numVects() {
        return vects.size();
    }

    public Vect getVect(int index) throws IndexOutOfBoundsException {
        return vects.getVect(index);
    }

    public VectBuilder getVect(int index, VectBuilder target) throws IndexOutOfBoundsException, NullPointerException {
        return vects.getVect(index, target);
    }

    public Line getLine(int index) throws IndexOutOfBoundsException, NullPointerException {
        return vects.getLine(index);
    }

    public double getX(int index) throws IndexOutOfBoundsException {
        return vects.getX(index);
    }

    public double getY(int index) {
        return vects.getY(index);
    }

    @Override
    public Rect getBounds() {
        return vects.getBounds();
    }

    @Override
    public Geom transform(Transform transform) throws NullPointerException {
        VectList transformed = vects.clone();
        transformed.transform(transform);
        return new MultiPoint(transformed);
    }

    @Override
    public PathIterator pathIterator() {
        final VectList v = (vects.size() == 1) ? new VectList().add(vects, 0).add(vects, 0) : vects;
        return new PathIterator() {
            int index = 0;

            @Override
            public int getWindingRule() {
                return WIND_NON_ZERO;
            }

            @Override
            public boolean isDone() {
                return (index >= v.size());
            }

            @Override
            public void next() {
                if (index < v.size()) {
                    index++;
                }
            }

            @Override
            public int currentSegment(float[] coords) {
                if (index == 0) {
                    coords[0] = (float) v.getX(0);
                    coords[1] = (float) v.getY(0);
                    return SEG_MOVETO;
                } else {
                    coords[0] = (float) v.getX(index);
                    coords[1] = (float) v.getY(index);
                    return SEG_LINETO;
                }
            }

            @Override
            public int currentSegment(double[] coords) {
                if (index == 0) {
                    coords[0] = v.getX(0);
                    coords[1] = v.getY(0);
                    return SEG_MOVETO;
                } else {
                    coords[0] = v.getX(index);
                    coords[1] = v.getY(index);
                    return SEG_LINETO;
                }
            }

        };
    }

    @Override
    public MultiPoint clone() {
        return this;
    }

    @Override
    public void toString(Appendable appendable) throws NullPointerException, GeomException {
        vects.toString(appendable);
    }

    @Override
    public void addTo(Network network, Tolerance tolerance) throws NullPointerException, IllegalArgumentException {
        network.addAllVertices(vects);
    }

    @Override
    public Geom buffer(double amt, Tolerance flatness, Tolerance tolerance) throws IllegalArgumentException, NullPointerException {
        if(amt < 0){
            return null;
        }else if(amt == 0){
            return this;
        }
        
        //Create a buffer at 0
        VectList point = new VectList();
        double angleSize = 2 * Math.PI;
        double sy = amt;
        Vect.linearizeArcInternal(0, 0, angleSize, 0, sy, 0, sy, amt, flatness.getTolerance(), point);
        
        Network network = new Network();
        int s = point.size()-1;
        for(int v = vects.size(); v-- > 0;){
            double x = vects.getX(v);
            double y = vects.getY(v);
            double bx = point.getX(s) + x;
            double by = point.getY(s) + y;
            for(int i = s; i-- > 0;){
                double ax = point.getX(i) + x;
                double ay = point.getY(i) + y;
                network.addLinkInternal(ax, ay, bx, by);
                bx = ax;
                by = ay;
            }
        }
        
        network.explicitIntersections(tolerance);
        removeWithinBuffer(vects, network, amt, flatness, tolerance);
        
        return RingSet.valueOf(network);
    }
    
    //remove any link from network with a mid point closer than the amt to one of the lines in this
    static void removeWithinBuffer(VectList vects, Network network, double amt, Tolerance flatness, Tolerance tolerance){
        final SpatialNode<Line> lines = network.getLinks();
        double threshold = amt - flatness.tolerance;
        final NearLinkRemover remover = new NearLinkRemover(threshold, network);
        RectBuilder bounds = new RectBuilder();
        for(int i = vects.size(); i-- > 0;){
            double y = vects.getX(i);
            double x = vects.getX(i);
            bounds.reset().add(x, y).buffer(threshold);
            remover.reset(x, y);
            lines.forOverlapping(bounds.build(), remover);
        }
    }
    
    @Override
    public Relate relate(Vect vect, Tolerance tolerance) throws NullPointerException {
        return relateInternal(vect.x, vect.y, tolerance);
    }

    @Override
    public Relate relate(VectBuilder vect, Tolerance tolerance) throws NullPointerException {
        return relateInternal(vect.getX(), vect.getY(), tolerance);
    }

    Relate relateInternal(double x, double y, Tolerance tolerance) throws NullPointerException {
        if (vects.getBounds().relateInternal(x, y, tolerance) == Relate.OUTSIDE) {
            return Relate.OUTSIDE;
        }
        for (int v = vects.size(); v-- > 0;) {
            if (tolerance.match(vects.getX(v), vects.getY(v), x, y)) {
                return Relate.TOUCH;
            }
        }
        return Relate.OUTSIDE;
    }

    @Override
    public Geom union(Geom other, Tolerance flatness, Tolerance tolerance) throws NullPointerException {
        if(getBounds().isDisjoint(other.getBounds())){
            return GeomSet.normalizedValueOf(this, other);
        }else{
            return Network.union(flatness, tolerance, this, other);
        }
    }

    @Override
    public Geom intersection(Geom other, Tolerance flatness, Tolerance tolerance) throws NullPointerException {
        if(getBounds().buffer(tolerance.tolerance).isDisjoint(other.getBounds())){
            return null;
        }
        VectList intersection = new VectList(vects.size());
        VectBuilder vect = new VectBuilder();
        for(int i = 0; i < vects.size(); i++){
            vects.getVect(i, vect);
            if(other.relate(vect, tolerance) != Relate.OUTSIDE){
                intersection.add(vect);
            }
        }
        return valueOf(intersection);
    }

    @Override
    public Geom less(Geom other, Tolerance flatness, Tolerance tolerance) throws NullPointerException {
        Rect otherBounds = other.getBounds().buffer(tolerance.tolerance);
        if(getBounds().isDisjoint(otherBounds)){
            return this;
        }
        VectList ret = new VectList(vects.size());
        VectBuilder vect = new VectBuilder();
        for(int i = 0; i < vects.size(); i++){
            vects.getVect(i, vect);
            if(other.relate(vect, tolerance) == Relate.INSIDE){
                ret.add(vect);
            }
        }
        return valueOf(ret);
    }
    
    static class NearLinkRemover implements NodeProcessor<Line> {

        final double thresholdSq;
        final Network network;
        double x;
        double y;

        public NearLinkRemover(double threshold, Network network) {
            this.thresholdSq = threshold * threshold;
            this.network = network;
        }

        void reset(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean process(Rect bounds, Line j) {
            double jx = (j.ax + j.bx) / 2;
            double jy = (j.ay + j.by) / 2;
            if (Vect.distSq(x, y, jx, jy) < thresholdSq) {
                network.removeLink(j);
            }
            return true;
        }
    }

}
