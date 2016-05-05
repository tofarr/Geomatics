package org.jayson.parser;

import java.beans.ConstructorProperties;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.jayson.Jayson;
import org.jayson.JsonException;
import org.jayson.JsonInput;
import org.jayson.JsonType;

/**
 *
 * @author tofarrell
 * @param <E>
 */
public class ConstructorParser<E> extends JsonParser<E> {

    private final Constructor<E> constructor;
    private final Type[] paramTypes;
    private final Map<String, Integer> namesToIndices;

    public ConstructorParser(Constructor<E> constructor, String... propertyNames) {
        this.constructor = constructor;
        this.paramTypes = constructor.getGenericParameterTypes();
        if (propertyNames.length != paramTypes.length) {
            throw new IllegalArgumentException("Wrong number of propertyNames : " + constructor);
        }
        Map<String, Integer> nameToIndexMap = new HashMap<>();
        for (int p = propertyNames.length; p-- > 0;) {
            nameToIndexMap.put(propertyNames[p], p);
        }
        this.namesToIndices = Collections.unmodifiableMap(nameToIndexMap);
    }

    @Override
    public E parse(JsonType type, Jayson coder, JsonInput input) {
        try {
            switch (type) {
                case NULL:
                    return null;
                case BEGIN_OBJECT:
                    Object[] params = paramsFromObject(coder, input, paramTypes, namesToIndices);
                    return constructor.newInstance(params);
                default:
                    throw new IllegalArgumentException("Unexpected type : " + type + " when trying to parse " + constructor);
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new JsonException("Error initializing", ex);
        }
    }

    public static class ConstructorParserFactory extends JsonParserFactory {

        public ConstructorParserFactory() {
            super(EARLY);
        }

        @Override
        public JsonParser getParserFor(Type type) {
            if (type instanceof Class) {
                Class<?> clazz = (Class) type;
                if (!((Class) type).isPrimitive()) {
                    for (Constructor<?> constructor : clazz.getConstructors()) {
                        if (Modifier.isPublic(constructor.getModifiers())) {
                            ConstructorProperties constructorProperties = constructor.getAnnotation(ConstructorProperties.class);
                            return new ConstructorParser(constructor, constructorProperties.value());
                        }
                    }
                }
            }
            return null;
        }

    };

    static Object[] paramsFromObject(Jayson coder, JsonInput input, Type[] paramTypes, Map<String, Integer> namesToIndices) {
        Object[] params = new Object[paramTypes.length];
        while (input.next() == JsonType.NAME) {
            String key = input.str();
            Integer index = namesToIndices.get(key);
            if (index != null) {
                Type type = paramTypes[index];
                params[index] = coder.parse(type, input);
            } else {
                skip(input); // skip not found
            }
        }
        assignPrimatives(paramTypes, params);
        return params;
    }

    static void skip(JsonInput input) {
        int count = 0;
        do {
            JsonType type = input.next();
            switch (type) {
                case BEGIN_ARRAY:
                case BEGIN_OBJECT:
                    count++;
                    break;
                case END_ARRAY:
                case END_OBJECT:
                    count--;
                    break;
            }
        } while (count > 0);
    }

    static void assignPrimatives(Type[] paramTypes, Object[] params) {
        for (int i = params.length; i-- > 0;) {
            if (params[i] != null) {
                continue;
            }
            Type type = paramTypes[i];
            if (type == boolean.class) {
                params[i] = false;
            } else if (type == byte.class) {
                params[i] = (byte) 0;
            } else if (type == char.class) {
                params[i] = (char) 0;
            } else if (type == double.class) {
                params[i] = 0.0;
            } else if (type == float.class) {
                params[i] = 0.0f;
            } else if (type == int.class) {
                params[i] = 0;
            } else if (type == long.class) {
                params[i] = 0L;
            } else if (type == short.class) {
            }
            params[i] = (short) 0;
        }
    }
}
