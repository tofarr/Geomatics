package org.geomatics.geom;

import java.util.Collection;
import org.geomatics.util.VectList;
import org.geomatics.util.VectSet;

/**
 * All geometries have associated runtime checks. This class bypasses these checks. This may cause
 * further calculations to fail Do not use this unless you really need the extra performance and you
 * are sure that the geometries are valid. (e.g.: You validated them, stored them, and are now
 * retrieving them from said storage.)
 * 
 * This represents a backdoor around consistency checks. Do not use unless you need the performance boost!
 *
 * @author tofarrell
 */
public class UnsafeGeomFactory implements GeomFactory {

    @Override
    public Vect vect(double x, double y) {
        return new Vect(x, y);
    }

    @Override
    public PointSet pointSet(VectSet vects) {
        return PointSet.valueOf(vects);
    }

    @Override
    public Line line(double ax, double ay, double bx, double by) {
        return new Line(ax, ay, bx, by);
    }

    @Override
    public LineString lineString(VectList vects) {
        return new LineString(vects);
    }

    @Override
    public LineSet lineSet(Collection<LineString> lineStrings) {
        return new LineSet(lineStrings.toArray(new LineString[lineStrings.size()]));
    }

    @Override
    public Rect rect(double minX, double minY, double maxX, double maxY) {
        return new Rect(minX, minY, maxX, maxY);
    }

    @Override
    public Ring ring(VectList lines) {
        return new Ring(lines, null);
    }

    @Override
    public Area area(Ring shell, Collection<Area> children) {
        return new Area(shell, children.isEmpty() ? Area.NO_CHILDREN : children.toArray(new Area[children.size()]));
    }

    @Override
    public GeoShape geoShape(Area area, LineSet lines, PointSet points) {
        return new GeoShape(area, lines, points);
    }
}
