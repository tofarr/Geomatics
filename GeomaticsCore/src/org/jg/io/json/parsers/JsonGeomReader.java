package org.jg.io.json.parsers;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Logger;
import org.jg.geom.Geom;
import org.jg.geom.GeomIOException;
import org.jsonutil.JsonGeomParser;
import org.jayson.JsonReader;
import org.jayson.JsonType;

/**
 * Standard Geom reader is based on Gson.
 *
 * @author tofarrell
 */
public class JsonGeomReader {

    private static final Logger LOG = Logger.getLogger(JsonGeomReader.class.getName());

    public static final JsonGeomReader DEFAULT;

    static {
        ServiceLoader<JsonGeomParser> loader = ServiceLoader.load(JsonGeomParser.class);
        List<JsonGeomParser> parsers = new ArrayList<>();
        for (JsonGeomParser parser : loader) {
            parsers.add(parser);
        }
        DEFAULT = new JsonGeomReader(parsers.toArray(new JsonGeomParser[parsers.size()]));

    }
    private final Map<String, JsonGeomParser> parsers;

    public JsonGeomReader(JsonGeomParser... parserArray) throws IllegalArgumentException, NullPointerException {
        if (parserArray.length == 0) {
            throw new IllegalArgumentException("No parsers defined!");
        }
        HashMap<String, JsonGeomParser> parserMap = new HashMap<>();
        for (JsonGeomParser parser : parserArray) {
            parserMap.put(parser.getCode(), parser);
        }
        parsers = Collections.unmodifiableMap(new HashMap<>(parserMap));
    }

    /**
     * Get immutable collection of parsers
     *
     * @return immutable collection of parsers
     */
    public Map<String, JsonGeomParser> getParsers() {
        return parsers;
    }

    public Geom read(Reader reader) throws GeomIOException {
        JsonReader jsonReader = new JsonReader(reader);
        return read(jsonReader);
    }

    public Geom read(JsonReader reader) throws GeomIOException {
        JsonType type = reader.next();
        if (type != JsonType.BEGIN_ARRAY) {
            throw new GeomIOException("Unexpected type : " + type);
        }
        type = reader.next();
        if(type != JsonType.STRING){
            throw new GeomIOException("Unexpected type : " + type);
        }
        String geomType = reader.str();
        JsonGeomParser parser = parsers.get(geomType);
        if(parser == null){
            throw new GeomIOException("Unknown type : "+geomType);
        }
        Geom ret = parser.parse(reader);
        return ret;
    }
}
