package org.jsonutil.parser;

import java.lang.reflect.Type;
import org.jsonutil.JsonCoder;
import org.jsonutil.JsonException;
import org.jsonutil.JsonInput;
import org.jsonutil.JsonType;

/**
 *
 * @author tofarrell
 */
public class BooleanParser extends JsonParser<Boolean> {

    public static final BooleanParser INSTANCE = new BooleanParser();

    private BooleanParser() {
    }

    @Override
    public Boolean parse(JsonType type, JsonCoder coder, JsonInput input) {
        switch (type) {
            case BOOLEAN:
                return input.bool();
            case NULL:
                return null;
            case NUMBER:
                return input.num() != 0;
            case STRING:
                return !("false".equalsIgnoreCase(input.str()) || input.str().isEmpty() || "0".equals(input.str()));
            default:
                throw new JsonException("Expected BOOLEAN, found " + type);
        }
    }

    public static class BooleanParserFactory extends JsonParserFactory {

        public BooleanParserFactory() {
            super(LATE);
        }

        @Override
        public JsonParser getParserFor(Type type) throws JsonException {
            return (type == boolean.class || type == Boolean.class) ? INSTANCE : null;
        }

    }
}
