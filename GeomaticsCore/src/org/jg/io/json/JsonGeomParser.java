package org.jg.io.json;

import org.jg.geom.Geom;
import org.jg.geom.GeomIOException;

/**
 *
 * @author tofarrell
 */
public interface JsonGeomParser<G extends Geom> {

    public String getCode();

    public G parse(JsonReader reader) throws GeomIOException;
}
