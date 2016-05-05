package org.jayson.parser;

import java.lang.reflect.Type;
import org.jayson.Jayson;
import org.jayson.JaysonException;
import org.jayson.JaysonInput;
import org.jayson.JaysonType;

/**
 *
 * @author tofarrell
 */
public class BooleanParser extends JaysonParser<Boolean> {

    public static final BooleanParser INSTANCE = new BooleanParser();

    private BooleanParser() {
    }

    @Override
    public Boolean parse(JaysonType type, Jayson coder, JaysonInput input) {
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
                throw new JaysonException("Expected BOOLEAN, found " + type);
        }
    }

    public static class BooleanParserFactory extends JaysonParserFactory {

        public BooleanParserFactory(int priority) {
            super(priority);
        }

        @Override
        public JaysonParser getParserFor(Type type) throws JaysonException {
            return (type == boolean.class || type == Boolean.class) ? INSTANCE : null;
        }

    }
}
