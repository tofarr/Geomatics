package org.jg.io.json.parsers;

import org.jg.geom.GeomFactory;
import org.jg.geom.GeomIOException;
import org.jg.geom.LineString;
import org.jg.io.json.JsonGeomParser;
import org.jg.io.json.JsonReader;
import org.jg.util.VectList;

/**
 *
 * @author tofarrell
 */
public class LineStringParser implements JsonGeomParser<LineString> {

    private final GeomFactory factory;

    public LineStringParser(GeomFactory factory) throws NullPointerException {
        if (factory == null) {
            throw new NullPointerException("Factory must not be null!");
        }
        this.factory = factory;
    }

    @Override
    public String getCode() {
        return LineString.CODE;
    }

    @Override
    public LineString parse(JsonReader reader) throws GeomIOException {
        VectList vects = VectListParser.INSTANCE.parse(reader);
        LineString ret = factory.lineString(vects);
        return ret;
    }
}
