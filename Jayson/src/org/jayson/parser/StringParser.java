package org.jayson.parser;

import java.lang.reflect.Type;
import org.jayson.Jayson;
import org.jayson.JsonException;
import org.jayson.JsonInput;
import org.jayson.JsonType;
import static org.jayson.JsonType.BOOLEAN;
import static org.jayson.JsonType.NULL;
import static org.jayson.JsonType.NUMBER;
import static org.jayson.JsonType.STRING;

/**
 *
 * @author tofarrell
 */
public final class StringParser extends JsonParser<String> {

    public static final StringParser INSTANCE = new StringParser();

    private StringParser() {
    }

    @Override
    public String parse(JsonType type, Jayson coder, JsonInput input) {
        switch (type) {
            case BOOLEAN:
                return Boolean.toString(input.bool());
            case NULL:
                return null;
            case NUMBER:
                return Double.toString(input.num());
            case STRING:
                return input.str();
            default:
                throw new JsonException("Expected STRING, found " + type);
        }
    }

    public static class StringParserFactory extends JsonParserFactory {

        public StringParserFactory() {
            super(LATE);
        }

        @Override
        public JsonParser getParserFor(Type type) throws JsonException {
            return (type == String.class) ? INSTANCE : null;
        }

    }
}
