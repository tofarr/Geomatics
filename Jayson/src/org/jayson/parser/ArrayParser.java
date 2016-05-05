package org.jayson.parser;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.jayson.Jayson;
import org.jayson.JaysonInput;
import org.jayson.JaysonType;

/**
 *
 * @author tofarrell
 */
public class ArrayParser<E> extends JaysonParser<E> {

    private final org.jayson.parser.CollectionParser parser;

    public ArrayParser(org.jayson.parser.CollectionParser parser) {
        this.parser = parser;
    }

    @Override
    public E parse(JaysonType type, Jayson coder, JaysonInput input) {
        Collection collection = parser.parse(type, coder, input);
        Object array = Array.newInstance((Class) parser.contentType, collection.size());
        Iterator iter = collection.iterator();
        for(int i = 0; i < collection.size(); i++){
            Array.set(array, i, iter.next());
        }
        return (E) array;
    }

    public static class ArrayParserFactory extends JaysonParserFactory {

        public ArrayParserFactory(int priority) {
            super(priority);
        }

        @Override
        public JaysonParser getParserFor(Type type) {
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
