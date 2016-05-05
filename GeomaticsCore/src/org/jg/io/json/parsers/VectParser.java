package org.jg.io.json.parsers;

import org.jg.geom.GeomFactory;
import org.jg.geom.GeomIOException;
import org.jg.geom.Vect;
import org.jsonutil.JsonGeomParser;
import org.jayson.JaysonReader;
import org.jayson.JaysonType;

/**
 *
 * @author tofarrell
 */
public final class VectParser implements JsonGeomParser<Vect> {

    private final GeomFactory factory;

    public VectParser(GeomFactory factory) throws NullPointerException {
        if (factory == null) {
            throw new NullPointerException("Factory must not be null!");
        }
        this.factory = factory;
    }

    @Override
    public String getCode() {
        return Vect.CODE;
    }

    @Override
    public Vect parse(JaysonReader reader) throws GeomIOException {
        Vect ret = factory.vect(reader.nextNum(), reader.nextNum());
        if (reader.next() != JaysonType.END_ARRAY) {
            throw new GeomIOException("Point must not contain additional data");
        }
        return ret;
    }

}
