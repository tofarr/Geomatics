package org.geomatics.geom;

import org.geomatics.geom.io.GeomJaysonWriter;
import org.geomatics.geom.io.PathHandler;
import org.geomatics.util.Tolerance;
import org.geomatics.util.Transform;
import org.geomatics.util.VectList;

/**
 *
 * @author tofar
 */
public class Path implements Geom {

    public static final String CODE = "PH";
    private final VectList ordinates;
    private final PathSegType[] segs;

    Path(VectList ordinates, PathSegType... segs) {
        this.ordinates = ordinates;
        this.segs = segs;
    }
    
    public int numSegs(){
        return segs.length;
    }
    
    public PathSegType typeAt(int index) throws IndexOutOfBoundsException{
        return segs[index];
    }
    
    public void ordinatesAt(int index, VectList result) throws IndexOutOfBoundsException{
        int ordIndex = 0;
        for(int i = 0; i < index; i++){
            PathSegType seg = segs[i];
            switch(seg){
                case CLOSE:
                    break;
                case LINE:
                case MOVE:
                    ordIndex++;
                    break;
                case QUAD:
                    ordIndex+=2;
                    break;
                case CUBIC:
                    ordIndex+=3;
                    break;
            }
        }
        PathSegType seg = segs[index];
        switch(seg){
            case CLOSE:
                break;
            case LINE:
            case MOVE:
                result.clear().addAll(ordinates, ordIndex, 1);
                break;
            case QUAD:
                result.clear().addAll(ordinates, ordIndex, 2);
                break;
            case CUBIC:
                result.clear().addAll(ordinates, ordIndex, 3);
                break;
        }
    }
    
    public PathBuilder toBuilder(){
        return new PathBuilder();
    }

    @Override
    public Rect getBounds() {
        return ordinates.getBounds();
    }

    @Override
    public Path transform(Transform transform) throws NullPointerException {
        if (transform.mode == Transform.NO_OP) {
            return this;
        }
        VectList ords = ordinates.clone();
        ords.transform(transform);
        return new Path(ords, segs);
    }

    @Override
    public PathIter iterator() {
        return new PathIter() {
            int ordIndex;
            int segIndex;
            double ox;
            double oy;

            PathSegType seg = (segs.length > 0) ? segs[0] : null;

            @Override
            public boolean isDone() {
                return segIndex >= segs.length;
            }

            @Override
            public void next() {
                if (segIndex >= segs.length) {
                    throw new IllegalStateException();
                }
                switch (seg) {
                    case MOVE:
                        ox = ordinates.getX(ordIndex);
                        oy = ordinates.getY(ordIndex);
                        ordIndex++;
                        break;
                    case LINE:
                        ordIndex++;
                        break;
                    case QUAD:
                        ordIndex += 2;
                        break;
                    case CUBIC:
                        ordIndex += 3;
                        break;
                    case CLOSE:
                        break;
                }
                segIndex++;
                if (!isDone()) {
                    seg = segs[segIndex];
                }
            }

            @Override
            public PathSegType currentSegment(double[] coords) throws IllegalStateException {
                switch (seg) {
                    case MOVE:
                    case LINE:
                        ordinates.getOrds(ordIndex, coords, 0, 1);
                        break;
                    case QUAD:
                        ordinates.getOrds(ordIndex, coords, 0, 2);
                        break;
                    case CUBIC:
                        ordinates.getOrds(ordIndex, coords, 0, 3);
                        break;
                    case CLOSE:
                        coords[0] = ox;
                        coords[1] = oy;
                        break;
                }
                return seg;
            }
        };
    }

    @Override
    public Path clone() {
        return this;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        new PathHandler().render(this, new GeomJaysonWriter(str));
        return str.toString();
    }
    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.ordinates.hashCode();
        hash = 29 * hash + this.segs.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Path) {
            Path other = (Path) obj;
            return this.ordinates.equals(other.ordinates) && this.segs.equals(other.segs);
        }
        return false;
    }

    @Override
    public GeoShape toGeoShape(Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        Network network = new Network();
        addTo(network, linearizer, accuracy);
        return GeoShape.valueOf(network, accuracy);
    }

    @Override
    public void addTo(Network network, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        int ordIndex = 0;
        double ox = 0;
        double oy = 0;
        double px = 0;
        double py = 0;
        VectList curve = new VectList();
        for (int segIndex = 0; segIndex < segs.length; segIndex++) {
            switch (segs[segIndex]) {
                case MOVE:
                    px = ox = ordinates.getX(ordIndex);
                    py = oy = ordinates.getY(ordIndex);
                    ordIndex++;
                    break;
                case LINE:
                    double x = ordinates.getX(ordIndex);
                    double y = ordinates.getY(ordIndex);
                    ordIndex++;
                    network.addLinkInternal(px, py, x, y);
                    px = x;
                    py = y;
                    break;
                case CLOSE:
                    network.addLinkInternal(px, py, ox, oy);
                    px = ox;
                    py = oy;
                    break;
                case QUAD: {
                    double ax = ordinates.getX(ordIndex);
                    double ay = ordinates.getY(ordIndex);
                    ordIndex++;
                    double bx = ordinates.getX(ordIndex);
                    double by = ordinates.getY(ordIndex);
                    ordIndex++;
                    curve.clear();
                    linearizer.bezierQuad(px, py, ax, ay, bx, by, curve);
                    network.addLink(px, py, curve.getX(0), curve.getY(0));
                    network.addAllLinks(curve);
                    px = curve.getX(curve.size() - 1);
                    py = curve.getY(curve.size() - 1);
                    break;
                }
                case CUBIC: {
                    double ax = ordinates.getX(ordIndex);
                    double ay = ordinates.getY(ordIndex);
                    ordIndex++;
                    double bx = ordinates.getX(ordIndex);
                    double by = ordinates.getY(ordIndex);
                    ordIndex++;
                    double cx = ordinates.getX(ordIndex);
                    double cy = ordinates.getY(ordIndex);
                    ordIndex++;
                    curve.clear();
                    linearizer.bezierCubic(px, py, ax, ay, bx, by, cx, cy, curve);
                    network.addLink(px, py, curve.getX(0), curve.getY(0));
                    network.addAllLinks(curve);
                    px = curve.getX(curve.size() - 1);
                    py = curve.getY(curve.size() - 1);
                    break;
                }
            }
        }
    }

    @Override
    public Geom buffer(double amt, Linearizer linearizer, Tolerance accuracy) throws IllegalArgumentException, NullPointerException {
        return toGeoShape(linearizer, accuracy).buffer(amt, linearizer, accuracy);
    }

    @Override
    public int relate(Vect vect, Tolerance accuracy) throws NullPointerException {
        return toGeoShape(Linearizer.DEFAULT, accuracy).relate(vect, accuracy);
    }

    @Override
    public int relate(VectBuilder vect, Tolerance accuracy) throws NullPointerException {
        return toGeoShape(Linearizer.DEFAULT, accuracy).relate(vect, accuracy);
    }

    @Override
    public int relate(Geom geom, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        return toGeoShape(linearizer, accuracy).relate(geom, linearizer, accuracy);
    }

    @Override
    public double getArea(Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        return toGeoShape(linearizer, accuracy).getArea(linearizer, accuracy);
    }

    @Override
    public Geom union(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        return toGeoShape(linearizer, accuracy).union(other, linearizer, accuracy);
    }

    @Override
    public Geom intersection(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        return toGeoShape(linearizer, accuracy).union(other, linearizer, accuracy);
    }

    @Override
    public Geom less(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        return toGeoShape(linearizer, accuracy).union(other, linearizer, accuracy);
    }

    @Override
    public Geom xor(Geom other, Linearizer linearizer, Tolerance accuracy) throws NullPointerException {
        return toGeoShape(linearizer, accuracy).union(other, linearizer, accuracy);
    }

}
