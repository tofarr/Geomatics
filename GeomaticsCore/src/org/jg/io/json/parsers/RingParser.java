package org.jg.io.json.parsers;

import org.jg.geom.GeomFactory;
import org.jg.geom.GeomIOException;
import org.jg.geom.LineString;
import org.jg.geom.Ring;
import org.jsonutil.JsonGeomParser;
import org.jayson.JsonReader;
import org.jg.util.VectList;

/**
 *
 * @author tofarrell
 */
public class RingParser implements JsonGeomParser<Ring> {

    private final GeomFactory factory;

    public RingParser(GeomFactory factory) throws NullPointerException {
        if (factory == null) {
            throw new NullPointerException("Factory must not be null!");
        }
        this.factory = factory;
    }

    @Override
    public String getCode() {
        return Ring.CODE;
    }

    @Override
    public Ring parse(JsonReader reader) throws GeomIOException {
        VectList vects = VectListParser.INSTANCE.parse(reader);
        Ring ret = factory.ring(vects);
        return ret;
    }
}
