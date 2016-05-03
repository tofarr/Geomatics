package org.jg.geom;

import java.util.Collection;
import org.jg.util.Tolerance;
import org.jg.util.VectList;
import org.jg.util.VectSet;

/**
 *
 * @author tofarrell
 */
public class DefaultGeomFactory implements GeomFactory {

    private final Tolerance accuracy;

    public DefaultGeomFactory(Tolerance accuracy) {
        this.accuracy = accuracy;
    }

    @Override
    public Vect vect(double x, double y) {
        return Vect.valueOf(x, y);
    }

    @Override
    public PointSet pointSet(VectSet vectSet) {
        return PointSet.valueOf(vectSet);
    }

    @Override
    public Line line(double ax, double ay, double bx, double by) {
        return Line.valueOf(ax, ay, bx, by);
    }

    @Override
    public LineString lineString(VectList vects) {
        return LineString.valueOf(Tolerance.ZERO, vects);
    }

    @Override
    public LineSet lineSet(Collection<LineString> lineStrings) {
        Network network = new Network();
        for(LineString lineString : lineStrings){
            lineString.addTo(network);
        }
        return LineSet.valueOf(accuracy, network);
    }

    @Override
    public Rect rect(double minX, double minY, double maxX, double maxY) {
        return Rect.valueOf(minX, minY, maxX, maxY);
    }

    @Override
    public Ring ring(VectList lines) {
        return Ring.valueOf(accuracy, lines);
    }

    @Override
    public Area area(Ring shell, Collection<Area> children) {
        Network network = new Network();
        if(shell != null){
            shell.addTo(network);
        }
        for(Area child : children){
            child.addTo(network);
        }
        return Area.valueOf(accuracy, network);
    }

    @Override
    public GeoShape geoShape(Area area, LineSet lines, PointSet points) {
        return GeoShape.valueOf(area, lines, points, accuracy);
    }

}
