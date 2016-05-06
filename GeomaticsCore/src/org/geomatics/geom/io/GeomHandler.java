package org.geomatics.geom.io;

import org.jayson.Jayson;
import org.jayson.JaysonException;
import org.jayson.JaysonInput;
import org.jayson.JaysonOutput;
import org.jayson.JaysonType;
import org.geomatics.geom.Geom;
import org.geomatics.geom.GeomFactory;

/**
 *
 * @author tofarrell
 * @param <G>
 */
public abstract class GeomHandler<G extends Geom> {

    public final String code;
    public final Class<G> type;

    public GeomHandler(String code, Class<G> type) {
        this.code = code;
        this.type = type;
    }
    
    public void render(G value, JaysonOutput out) throws JaysonException{
        out.beginArray().str(code);
        renderRemaining(value, out);
    }

    public abstract G parseRemaining(GeomFactory factory, JaysonInput input) throws JaysonException;

    public abstract void renderRemaining(G value, JaysonOutput out) throws JaysonException;
}
