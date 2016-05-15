package org.jayson.parser;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import org.jayson.Jayson;
import org.jayson.JaysonBuffer;
import org.jayson.JaysonException;
import org.jayson.JaysonInput;
import org.jayson.JaysonType;
import org.jayson.poly.PolymorphicMap;

/**
 *
 * @author tofar
 */
public class ObjectParser extends JaysonParser {

    private final PolymorphicMap polymorphicMap;

    public ObjectParser(PolymorphicMap polymorphicMap) {
        if (polymorphicMap == null) {
            throw new NullPointerException();
        }
        this.polymorphicMap = polymorphicMap;
    }

    @Override
    public Object parse(JaysonType type, Jayson coder, JaysonInput input) {
        try {
            switch (type) {
                case NULL:
                    return null;
                case STRING:
                    return input.str();
                case NUMBER:
                    return input.num();
                case BOOLEAN:
                    return input.bool();
                case BEGIN_ARRAY:
                    return coder.parse(List.class, type, input); // array defaults to list
                case BEGIN_OBJECT:
                    JaysonBuffer buffer = new JaysonBuffer();
                    buffer.beginObject();
                    buffer.copyRemaining(input);
                    JaysonInput typeInput = buffer.getInputAt("$type", 1);
                    if(typeInput != null){ // type specified!
                        String typeName = typeInput.nextStr();
                        Class implClass = polymorphicMap.getImplClass(typeName);
                        return coder.parse(implClass, buffer.getInput());
                    }
                    return coder.parse(Map.class, buffer.getInput());
                default:
                    throw new JaysonException("Unexpected type : " + type);
            }
        } catch (Exception ex) {
            throw new JaysonException("Error initializing", ex);
        }
    }

    public static class ObjectParserFactory extends JaysonParserFactory {

        private final ObjectParser parser;

        public ObjectParserFactory(int priority, PolymorphicMap polymorphicMap) {
            super(priority);
            this.parser = new ObjectParser(polymorphicMap);
        }

        @Override
        public JaysonParser getParserFor(Type type) {
            return parser; // THis can parse just about anything
        }
    };
}
