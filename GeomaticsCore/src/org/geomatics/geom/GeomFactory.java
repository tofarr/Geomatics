package org.geomatics.geom;

import java.util.Collection;
import org.geomatics.util.VectList;
import org.geomatics.util.VectSet;

/**
 *
 * @author tofarrell
 */
public interface GeomFactory {

    Vect vect(double x, double y);

    PointSet pointSet(VectSet vects);

    Line line(double ax, double ay, double bx, double by);

    LineString lineString(VectList vects);

    LineSet lineSet(Collection<LineString> lineStrings);

    Rect rect(double minX, double minY, double maxX, double maxY);

    Ring ring(VectList lines);

    Area area(Ring shell, Collection<Area> children);

    GeoShape geoShape(Area area, LineSet lines, PointSet points);
}
