package org.geomatics.geom.io;

import java.util.HashMap;
import java.util.Map;
import org.jayson.Jayson;
import org.jayson.JaysonException;
import org.jayson.JaysonInput;
import org.jayson.JaysonOutput;
import org.jayson.JaysonType;
import org.jayson.parser.JaysonParser;
import org.jayson.render.JaysonRender;
import org.geomatics.geom.Geom;
import org.geomatics.geom.GeomFactory;

/**
 *
 * @author tofarrell
 */
public class GeomJaysonifier extends JaysonParser<Geom> implements JaysonRender<Geom> {

    private final GeomFactory factory;
    private final Map<Class, GeomHandler> byType;
    private final Map<String, GeomHandler> byCode;
    

    public GeomJaysonifier(GeomFactory factory, GeomHandler... handlers) {
        this.factory = factory;
        this.byType = new HashMap<>();
        this.byCode = new HashMap<>();
        for(GeomHandler handler : handlers){
            this.byType.put(handler.type, handler);
            this.byCode.put(handler.code, handler);
        }
    }

    @Override
    public void render(Geom value, Jayson coder, JaysonOutput out) throws JaysonException {
        GeomHandler handler = byType.get(value.getClass());
        if(handler == null){
            throw new JaysonException("Unknown geometryType : "+value.getClass());
        }
        out.beginArray().str(handler.code);
        handler.renderRemaining(value, out);
    }

    @Override
    public Geom parse(JaysonType type, Jayson coder, JaysonInput input) throws JaysonException {
        if(type != JaysonType.BEGIN_ARRAY){
            throw new JaysonException("Expected BEGIN_ARRAY found "+type);
        }
        String code = input.nextStr();
        GeomHandler handler = byCode.get(code);
        if(handler == null){
            throw new JaysonException("Unknown geometryType : "+code);
        }
        Geom ret = handler.parseRemaining(factory, input);
        return ret;
    }

}
