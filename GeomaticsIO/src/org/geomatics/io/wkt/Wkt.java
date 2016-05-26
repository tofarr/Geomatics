package org.geomatics.io.wkt;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.geomatics.geom.Area;
import org.geomatics.geom.GeoShape;
import org.geomatics.geom.Geom;
import org.geomatics.geom.GeomFactory;
import org.geomatics.geom.LineSet;
import org.geomatics.geom.LineString;
import org.geomatics.geom.Linearizer;
import org.geomatics.geom.PointSet;
import org.geomatics.geom.Ring;
import org.geomatics.geom.Vect;
import org.geomatics.geom.io.GeomIOException;
import org.geomatics.util.Tolerance;
import org.geomatics.util.VectList;
import org.geomatics.util.VectSet;

/**
 *
 * @author tofar
 */
public class Wkt {

    private final Linearizer linearizer;
    private final Tolerance accuracy;
    private final GeomFactory factory;

    public Wkt(Linearizer linearizer, Tolerance accuracy, GeomFactory factory) {
        this.linearizer = linearizer;
        this.accuracy = accuracy;
        this.factory = factory;
    }

    public String toWkt(Geom geom) {
        return geom.toGeoShape(linearizer, accuracy).toWkt();
    }

    public void toWkt(Geom geom, Appendable appendable) {
        geom.toGeoShape(linearizer, accuracy).toWkt(appendable);
    }

    public Geom fromWkt(String wkt) throws GeomIOException {
        return fromWkt(new StringReader(wkt));

    }

    public Geom fromWkt(Reader reader) throws GeomIOException {
        WktInput input = new WktInput(reader);
        String type = input.nextToken();
        if (!"(".equals(input.nextToken())) {
            throw new GeomIOException("Expected '(' found '" + input.currentToken() + "'");
        }
        switch (type) {
            case "POINT":
                return readPoint(input);
            case "MULTIPOINT":
                return readMultiPoint(input);
            case "LINESTRING":
                return readLineString(input);
            case "MULTILINESTRING":
                return readMultiLineString(input);
            case "POLYGON":
                return readPolygon(input);
            case "MULTIPOLYGON":
                return readMultiPolygon(input);
            case "GEOMETRYCOLLECTION":
                return readGeometryCollection(input);
            default:
                throw new GeomIOException("Unsupported type : " + type);
        }
    }

    Vect readPoint(WktInput input) throws GeomIOException {
        double x = Double.parseDouble(input.nextToken());
        String token = input.nextToken();
        if (",".equals(token)) {
            token = input.nextToken();
        }
        double y = Double.parseDouble(token);
        if (!")".equals(input.nextToken())) {
            throw new GeomIOException("Expected ')' found '" + input.currentToken() + "'");
        }
        return factory.vect(x, y);
    }

    PointSet readMultiPoint(WktInput input) throws GeomIOException {
        return factory.pointSet(readVectSet(input));
    }

    LineString readLineString(WktInput input) throws GeomIOException {
        return factory.lineString(readVectList(input));
    }

    LineSet readMultiLineString(WktInput input) throws GeomIOException {
        Collection<LineString> lines = new ArrayList<>();
        while (true) {
            String token = input.nextToken();
            if (",".equals(token)) {
                if (lines.isEmpty()) {
                    throw new GeomIOException("Unexpected character : ','");
                }
            } else if ("(".equals(token)) {
                LineString ls = readLineString(input);
                lines.add(ls);
            } else if (")".equals(token)) {
                return factory.lineSet(lines);
            }
        }
    }

    Area readPolygon(WktInput input) throws GeomIOException {
        Ring shell = factory.ring(readVectList(input));
        List<Area> holes = new ArrayList<>();
        while (true) {
            String token = input.nextToken();
            if (",".equals(token)) {
                if (holes.isEmpty()) {
                    throw new GeomIOException("Unexpected character : ','");
                }
            } else if ("(".equals(token)) {
                Ring ring = factory.ring(readVectList(input));
                holes.add(new Area(ring));
            } else if (")".equals(token)) {
                return factory.area(shell, holes);
            }
        }
    }

    Area readMultiPolygon(WktInput input) throws GeomIOException {
        List<Area> polygons = new ArrayList<>();
        while (true) {
            String token = input.nextToken();
            if (",".equals(token)) {
                if (polygons.isEmpty()) {
                    throw new GeomIOException("Unexpected character : ','");
                }
            } else if ("(".equals(token)) {
                Area polygon = readPolygon(input);
                polygons.add(polygon);
            } else if (")".equals(token)) {
                return factory.area(null, polygons);
            }
        }
    }

    Geom readGeometryCollection(WktInput input) throws GeomIOException {
        GeoShape ret = null;
        while (true) {
            String token = input.nextToken();
            if (",".equals(token)) {
                if (ret == null) {
                    throw new GeomIOException("Unexpected character : ','");
                }
            } else if ("(".equals(token)) {
                GeoShape geoShape = readGeometryCollection(input).toGeoShape(linearizer, accuracy);
                ret = (ret == null) ? null : ret.xor(ret, accuracy);
            } else if (")".equals(token)) {
                return ret;
            }
        }
    }

    VectList readVectList(WktInput input) throws GeomIOException {
        VectList ret = new VectList();
        String token = input.nextToken();
        while (true) {
            double x = Double.parseDouble(token);
            token = input.nextToken();
            if (",".equals(token)) {
                token = input.nextToken();
            }
            double y = Double.parseDouble(token);
            ret.add(x, y);
            token = input.nextToken();
            if (")".equals(token)) {
                return ret;
            } else if (",".equals(token)) {
                token = input.nextToken();
            }
        }
    }

    VectSet readVectSet(WktInput input) throws GeomIOException {
        VectSet ret = new VectSet();
        String token = input.nextToken();
        while (true) {
            double x = Double.parseDouble(token);
            token = input.nextToken();
            if (",".equals(token)) {
                token = input.nextToken();
            }
            double y = Double.parseDouble(token);
            ret.add(x, y);
            token = input.nextToken();
            if (")".equals(token)) {
                return ret;
            } else if (",".equals(token)) {
                token = input.nextToken();
            }
        }
    }
}
