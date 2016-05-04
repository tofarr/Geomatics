package org.jg.io.json.parsers;

import org.jg.geom.GeomFactory;
import org.jg.geom.GeomIOException;
import org.jg.geom.Line;
import org.jsonutil.JsonGeomParser;
import org.jsonutil.JsonReader;
import org.jsonutil.JsonType;

/**
 *
 * @author tofarrell
 */
public final class LineParser implements JsonGeomParser<Line> {

    private final GeomFactory factory;

    public LineParser(GeomFactory factory) throws NullPointerException {
        if (factory == null) {
            throw new NullPointerException("Factory must not be null!");
        }
        this.factory = factory;
    }


    @Override
    public String getCode() {
        return Line.CODE;
    }

    @Override
    public Line parse(JsonReader reader) throws GeomIOException {
        Line ret = factory.line(reader.nextNum(), reader.nextNum(), reader.nextNum(), reader.nextNum());
        if (reader.next() != JsonType.END_ARRAY) {
            throw new GeomIOException("Line must not contain additional data");
        }
        return ret;
    }

}
