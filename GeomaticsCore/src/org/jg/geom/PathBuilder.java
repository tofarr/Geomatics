package org.jg.geom;

import java.util.ArrayList;
import java.util.List;
import org.jg.util.VectList;

/**
 *
 * @author tofar
 */
public class PathBuilder {

    private final List<PathSegType> segs;
    private final VectList ordinates;

    public PathBuilder() {
        segs = new ArrayList<>();
        ordinates = new VectList();
    }

    PathBuilder(List<PathSegType> segs, VectList ords) {
        this.segs = segs;
        this.ordinates = ords;
    }

    public PathBuilder moveTo(Vect vect) throws NullPointerException {
        ordinates.add(vect);
        segs.add(PathSegType.MOVE);
        return this;
    }

    public PathBuilder moveToBuilder(VectBuilder vect) throws NullPointerException {
        ordinates.add(vect);
        segs.add(PathSegType.MOVE);
        return this;
    }

    public PathBuilder moveToList(VectList vects, int index) throws NullPointerException, IndexOutOfBoundsException {
        ordinates.add(vects, index);
        segs.add(PathSegType.MOVE);
        return this;
    }

    public PathBuilder moveToOrds(double x, double y) throws IllegalArgumentException {
        ordinates.add(x, y);
        segs.add(PathSegType.MOVE);
        return this;
    }

    public PathBuilder lineTo(Vect vect) throws NullPointerException {
        ordinates.add(vect);
        segs.add(PathSegType.LINE);
        return this;
    }

    public PathBuilder lineToBuilder(VectBuilder vect) throws NullPointerException {
        ordinates.add(vect);
        segs.add(PathSegType.LINE);
        return this;
    }

    public PathBuilder lineToList(VectList vects, int index, int numVects) throws NullPointerException, IndexOutOfBoundsException {
        ordinates.addAll(vects, index, numVects);
        while (numVects-- > 0) {
            segs.add(PathSegType.LINE);
        }
        return this;
    }

    public PathBuilder lineToOrds(double x, double y) throws IllegalArgumentException {
        ordinates.add(x, y);
        segs.add(PathSegType.LINE);
        return this;
    }

    public PathBuilder quadTo(Vect a, Vect b) throws NullPointerException {
        if (b == null) { // find problems before mutating
            throw new NullPointerException();
        }
        ordinates.add(a);
        ordinates.add(b);
        segs.add(PathSegType.QUAD);
        return this;
    }

    public PathBuilder quadToBuilder(VectBuilder a, VectBuilder b) throws NullPointerException {
        if (b == null) { // find problems before mutating
            throw new NullPointerException();
        }
        ordinates.add(a);
        ordinates.add(b);
        segs.add(PathSegType.QUAD);
        return this;
    }

    public PathBuilder quadToList(VectList vects, int index) {
        ordinates.addAll(vects, index, 2);
        segs.add(PathSegType.QUAD);
        return this;
    }

    public PathBuilder quadToOrds(double ax, double ay, double bx, double by) throws IllegalArgumentException {
        Vect.check(bx, by);
        ordinates.add(ax, ay);
        ordinates.add(bx, by);
        segs.add(PathSegType.QUAD);
        return this;
    }

    public PathBuilder cubicTo(Vect a, Vect b, Vect c) throws NullPointerException {
        if (b == null || c == null) { // find problems before mutating
            throw new NullPointerException();
        }
        ordinates.add(a);
        ordinates.add(b);
        ordinates.add(c);
        segs.add(PathSegType.CUBIC);
        return this;
    }

    public PathBuilder cubicToBuilder(VectBuilder a, VectBuilder b, VectBuilder c) throws NullPointerException {
        if (b == null || c == null) { // find problems before mutating
            throw new NullPointerException();
        }
        ordinates.add(a);
        ordinates.add(b);
        ordinates.add(c);
        segs.add(PathSegType.CUBIC);
        return this;
    }

    public PathBuilder cubicToList(VectList vects, int index) {
        ordinates.addAll(vects, index, 3);
        segs.add(PathSegType.CUBIC);
        return this;
    }

    public PathBuilder cubicToOrds(double ax, double ay, double bx, double by, double cx, double cy) {
        Vect.check(bx, by);
        Vect.check(cx, cy);
        ordinates.add(ax, ay);
        ordinates.add(bx, by);
        segs.add(PathSegType.QUAD);
        return this;
    }

    public Path build() {
        return new Path(ordinates.clone(), segs.toArray(new PathSegType[segs.size()]));
    }

}
