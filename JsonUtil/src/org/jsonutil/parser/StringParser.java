package org.jsonutil.parser;

import java.lang.reflect.Type;
import org.jsonutil.JsonCoder;
import org.jsonutil.JsonException;
import org.jsonutil.JsonInput;
import org.jsonutil.JsonType;
import static org.jsonutil.JsonType.BOOLEAN;
import static org.jsonutil.JsonType.NULL;
import static org.jsonutil.JsonType.NUMBER;
import static org.jsonutil.JsonType.STRING;

/**
 *
 * @author tofarrell
 */
public final class StringParser extends JsonParser<String> {

    public static final StringParser INSTANCE = new StringParser();

    private StringParser() {
    }

    @Override
    public String parse(JsonType type, JsonCoder coder, JsonInput input) {
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
