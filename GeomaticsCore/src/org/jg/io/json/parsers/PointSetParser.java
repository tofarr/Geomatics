package org.jg.io.json.parsers;

import org.jg.geom.GeomFactory;
import org.jg.geom.GeomIOException;
import org.jg.geom.PointSet;
import org.jsonutil.JsonGeomParser;
import org.jayson.JaysonReader;
import org.jayson.JaysonType;
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
    public PointSet parse(JaysonReader reader) throws GeomIOException {
        VectSet vects = new VectSet();
        while (reader.next() != JaysonType.END_ARRAY) {
            vects.add(reader.num(), reader.nextNum());
        }
        PointSet ret = factory.pointSet(vects);
        return ret;
    }

}
