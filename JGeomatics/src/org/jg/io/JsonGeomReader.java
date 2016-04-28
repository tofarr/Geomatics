package org.jg.io;

import com.google.gson.JsonArray;
import com.google.gson.stream.JsonReader;
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
        for(JsonGeomParser parser : parsers){
            
        }
        
    }
    private final Map<String, JsonGeomParser> parsers;

    public JsonGeomReader(JsonGeomParser... parsers) throws IllegalArgumentException, NullPointerException {
        if (parsers.length == 0) {
            throw new IllegalArgumentException("No parsers defined!");
        }
        HashMap<String, JsonGeomParser> parserMap = new HashMap<>();
        for(JsonGeomParser parser : parsers){
            
        }
        parsers = Collections.unmodifiableMap(new HashMap<>(parsers));
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

    }

    public Geom read(JsonReader reader) throws GeomIOException {

    }

    public Geom read(JsonArray array) throws GeomIOException {

    }

    public interface JsonGeomParser {

        public String getCode();

        public Geom parse(JsonReader reader) throws GeomIOException;
    }
}
