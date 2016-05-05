package org.jg.io.json.parsers;

import java.util.ArrayList;
import java.util.List;
import org.jg.geom.Area;
import org.jg.geom.GeomFactory;
import org.jg.geom.GeomIOException;
import org.jg.geom.LineSet;
import org.jg.geom.Ring;
import org.jsonutil.JsonGeomParser;
import org.jayson.JsonReader;
import org.jayson.JsonType;

/**
 *
 * @author tofarrell
 */
public class AreaParser implements JsonGeomParser<Area> {

    private final GeomFactory factory;
    private final RingParser parser;

    public AreaParser(GeomFactory factory, RingParser parser) throws NullPointerException {
        if (factory == null) {
            throw new NullPointerException("Factory must not be null!");
        }
        if (parser == null) {
            throw new NullPointerException("Parser must not be null!");
        }
        this.factory = factory;
        this.parser = parser;
    }

    @Override
    public String getCode() {
        return Area.CODE;
    }

    @Override
    public Area parse(JsonReader reader) throws GeomIOException {
        JsonType type = reader.next();
        Ring shell = null;
        if (type == JsonType.BEGIN_ARRAY) { // shell
            shell = parser.parse(reader);
        } else if (type != JsonType.NULL) {
            throw new GeomIOException("Unexpected type : " + type);
        }

        List<Area> children = new ArrayList<>();
        while (true) {
            type = reader.next();
            if (type == JsonType.END_ARRAY) {
                Area ret = factory.area(shell, children);
                return ret;
            } else if (type != JsonType.BEGIN_ARRAY) {
                throw new GeomIOException("BEGIN_ARRAY expected");
            }
            Area child = parse(reader);
            children.add(child);
        }
    }
}
