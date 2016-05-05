package org.jg.io.json.parsers;

import org.jg.geom.GeomFactory;
import org.jg.geom.GeomIOException;
import org.jg.geom.Rect;
import org.jsonutil.JsonGeomParser;
import org.jayson.JsonReader;
import org.jayson.JsonType;

/**
 *
 * @author tofarrell
 */
public class RectParser implements JsonGeomParser<Rect> {

    private final GeomFactory factory;


    public RectParser(GeomFactory factory) throws NullPointerException {
        if (factory == null) {
            throw new NullPointerException("Factory must not be null!");
        }
        this.factory = factory;
    }

    @Override
    public String getCode() {
        return Rect.CODE;
    }

    @Override
    public Rect parse(JsonReader reader) throws GeomIOException {
        Rect ret = factory.rect(reader.nextNum(), reader.nextNum(), reader.nextNum(), reader.nextNum());
        if (reader.next() != JsonType.END_ARRAY) {
            throw new GeomIOException("Line must not contain additional data");
        }
        return ret;
    }
}
