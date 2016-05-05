package org.jg.io.json.parsers;

import org.jg.geom.GeoShape;
import org.jg.geom.GeomIOException;
import org.jsonutil.JsonGeomParser;
import org.jayson.JaysonReader;

/**
 *
 * @author tofarrell
 */
public class GeoShapeParser implements JsonGeomParser<GeoShape> {

    private final AreaParser areaParser;
    private final LineSetParser lineSetParser;
    private final PointSetParser pointSetParser;

    public GeoShapeParser(AreaParser areaParser, LineSetParser lineSetParser, PointSetParser pointSetParser) throws NullPointerException {
        if (areaParser == null) {
            throw new NullPointerException("areaParser must not be null");
        }
        if (lineSetParser == null) {
            throw new NullPointerException("lineSetParser must not be null");
        }
        if (pointSetParser == null) {
            throw new NullPointerException("pointSetParser must not be null");
        }
        this.areaParser = areaParser;
        this.lineSetParser = lineSetParser;
        this.pointSetParser = pointSetParser;
    }

    @Override
    public String getCode() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GeoShape parse(JaysonReader reader) throws GeomIOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
