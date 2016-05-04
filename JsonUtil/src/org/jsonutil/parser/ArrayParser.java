package org.jsonutil.parser;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import org.jsonutil.JsonCoder;
import org.jsonutil.JsonInput;
import org.jsonutil.JsonType;

/**
 *
 * @author tofarrell
 */
public class ArrayParser<E> extends JsonParser<E> {

    private final org.jsonutil.parser.CollectionParser parser;

    public ArrayParser(org.jsonutil.parser.CollectionParser parser) {
        this.parser = parser;
    }

    @Override
    public E parse(JsonType type, JsonCoder coder, JsonInput input) {
        Collection collection = parser.parse(type, coder, input);
        Object[] array = (Object[]) Array.newInstance((Class) parser.contentType, collection.size());
        return (E) collection.toArray(array);
    }

    public static class ArrayParserFactory extends JsonParserFactory {

        public ArrayParserFactory() {
            super(LATE);
        }

        @Override
        public JsonParser getParserFor(Type type) {
            if (type instanceof Class) {
                Class clazz = (Class) type;
                if (clazz.isArray()) {
                    return new ArrayParser(new CollectionParser(ArrayList.class, clazz.getComponentType()));
                }
            }
            return null;
        }

    };
}
