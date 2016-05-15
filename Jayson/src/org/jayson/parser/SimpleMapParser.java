package org.jayson.parser;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import org.jayson.Jayson;
import org.jayson.JaysonException;
import org.jayson.JaysonInput;
import org.jayson.JaysonType;
import org.jayson.poly.ClassMap;
import org.jayson.poly.PolymorphicMap;

/**
 *
 * @author tofar
 * @param <E>
 */
public class SimpleMapParser<E extends Map> extends JaysonParser<E> {

    final Class<E> mapType;
    final Type keyType;
    final Type valueType;

    public SimpleMapParser(Class<E> mapType, Type keyType, Type valueType) {
        if (mapType == null || keyType == null || valueType == null) {
            throw new NullPointerException();
        }
        this.mapType = mapType;
        this.keyType = keyType;
        this.valueType = valueType;
    }

    @Override
    public E parse(JaysonType type, Jayson coder, JaysonInput input) {
        try {
            if (type == JaysonType.NULL) {
                return null;
            } else if (type != JaysonType.BEGIN_OBJECT) {
                throw new JaysonException("Expected object, found: " + type);
            }
            E map = mapType.newInstance();
            while (true) {
                type = input.next();
                if (type == JaysonType.END_OBJECT) {
                    break;
                }
                Object key = coder.parse(valueType, JaysonType.STRING, input);
                Object value = coder.parse(valueType, input);
                map.put(key, value);
            }
            return map;
        } catch (Exception ex) {
            throw new JaysonException("Error initializing", ex);
        }
    }

    public static class MapParserFactory extends JaysonParserFactory {

        private final PolymorphicMap polymorphicMap;

        public MapParserFactory(int priority, PolymorphicMap polymorphicMap) {
            super(priority);
            if (polymorphicMap == null) {
                throw new NullPointerException("polymorphicMap must not be null");
            }
            this.polymorphicMap = polymorphicMap;
        }

        @Override
        public JaysonParser getParserFor(Type type) {
            if(type instanceof Class){
                Class mapType = (Class) type;
                if (Map.class.isAssignableFrom(mapType)) {
                    mapType = getImpl(polymorphicMap, mapType);
                    return new SimpleMapParser(mapType, Object.class, Object.class);
                }
            }
            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) type;
                Type rawType = pt.getRawType();
                if (rawType instanceof Class) {
                    Class mapType = (Class) rawType;
                    if (Map.class.isAssignableFrom(mapType)) {
                        mapType = getImpl(polymorphicMap, mapType);
                        return new SimpleMapParser(mapType, pt.getActualTypeArguments()[0], pt.getActualTypeArguments()[1]) {};
                    }
                }
            }
            return null;
        }

        public static Class getImpl(PolymorphicMap polymorphicMap, Class clazz) {
            ClassMap classMap = polymorphicMap.getClassMap(clazz);
            if (classMap != null) {
                clazz = classMap.getImplClasses()[0];
            }
            return clazz;
        }
    };
}
