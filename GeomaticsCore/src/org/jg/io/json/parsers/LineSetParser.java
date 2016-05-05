package org.jg.io.json.parsers;

import java.util.ArrayList;
import java.util.List;
import org.jg.geom.GeomFactory;
import org.jg.geom.GeomIOException;
import org.jg.geom.LineSet;
import org.jg.geom.LineString;
import org.jsonutil.JsonGeomParser;
import org.jayson.JaysonReader;
import org.jayson.JaysonType;

/**
 *
 * @author tofarrell
 */
public class LineSetParser implements JsonGeomParser<LineSet> {

    private final GeomFactory factory;
    private final LineStringParser parser;

    public LineSetParser(GeomFactory factory, LineStringParser parser) throws NullPointerException {
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
        return LineSet.CODE;
    }

    @Override
    public LineSet parse(JaysonReader reader) throws GeomIOException {
        List<LineString> lineStrings = new ArrayList<>();
        while(true){
            JaysonType type = reader.next();
            if(type == JaysonType.END_ARRAY){
                LineSet ret = factory.lineSet(lineStrings);
                return ret;
            }else if(type != JaysonType.BEGIN_ARRAY){
                throw new GeomIOException("BEGIN_ARRAY expected");
            }
            LineString lineString = parser.parse(reader);
            lineStrings.add(lineString);
        }
    }
}
