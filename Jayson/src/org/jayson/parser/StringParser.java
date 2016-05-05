package org.jayson.parser;

import java.lang.reflect.Type;
import org.jayson.Jayson;
import org.jayson.JaysonException;
import org.jayson.JaysonInput;
import org.jayson.JaysonType;
import static org.jayson.JaysonType.BOOLEAN;
import static org.jayson.JaysonType.NULL;
import static org.jayson.JaysonType.NUMBER;
import static org.jayson.JaysonType.STRING;

/**
 *
 * @author tofarrell
 */
public final class StringParser extends JaysonParser<String> {

    public static final StringParser INSTANCE = new StringParser();

    private StringParser() {
    }

    @Override
    public String parse(JaysonType type, Jayson coder, JaysonInput input) {
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
                throw new JaysonException("Expected STRING, found " + type);
        }
    }

    public static class StringParserFactory extends JaysonParserFactory {

        public StringParserFactory(int priority) {
            super(priority);
        }

        @Override
        public JaysonParser getParserFor(Type type) throws JaysonException {
            return (type == String.class) ? INSTANCE : null;
        }

    }
}
