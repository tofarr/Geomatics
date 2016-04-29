package org.jg.io.json.parsers;

import org.jg.geom.GeomFactory;
import org.jg.geom.GeomIOException;
import org.jg.geom.PointSet;
import org.jg.io.json.JsonGeomParser;
import org.jg.io.json.JsonReader;
import org.jg.io.json.JsonType;
import org.jg.util.VectSet;

/**
 *
 * @author tofarrell
 */
public final class PointSetParser implements JsonGeomParser<PointSet> {

    private final GeomFactory factory;

    public PointSetParser(GeomFactory factory) throws NullPointerException {
        if (factory == null) {
            throw new NullPointerException("Factory must not be null!");
        }
        this.factory = factory;
    }
    
    @Override
    public String getCode() {
        return PointSet.CODE;
    }

    @Override
    public PointSet parse(JsonReader reader) throws GeomIOException {
        VectSet vects = new VectSet();
        while (reader.next() != JsonType.END_ARRAY) {
            vects.add(reader.num(), reader.nextNum());
        }
        PointSet ret = factory.pointSet(vects);
        return ret;
    }

}
