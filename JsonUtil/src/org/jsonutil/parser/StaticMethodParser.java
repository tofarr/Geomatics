package org.jsonutil.parser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.jsonutil.JsonCoder;
import org.jsonutil.JsonException;
import org.jsonutil.JsonInput;
import org.jsonutil.JsonType;
import static org.jsonutil.parser.ConstructorParser.paramsFromObject;

/**
 *
 * @author tofarrell
 */
public class StaticMethodParser<E> extends JsonParser<E> {

    private final Method initializer;
    private final Type[] paramTypes;
    private final Map<String, Integer> namesToIndices;

    public StaticMethodParser(Method initializer, String... propertyNames) {
        if (!Modifier.isPublic(initializer.getModifiers())) {
            throw new IllegalArgumentException("Method is not public " + initializer);
        }
        if (!Modifier.isStatic(initializer.getModifiers())) {
            throw new IllegalArgumentException("Method is not static " + initializer);
        }
        this.initializer = initializer;
        this.paramTypes = initializer.getGenericParameterTypes();
        if (propertyNames.length != paramTypes.length) {
            throw new IllegalArgumentException("Wrong number of propertyNames : " + initializer);
        }
        Map<String, Integer> nameToIndexMap = new HashMap<>();
        for (int p = propertyNames.length; p-- > 0;) {
            nameToIndexMap.put(propertyNames[p], p);
        }
        this.namesToIndices = Collections.unmodifiableMap(nameToIndexMap);
    }

    @Override
    public E parse(JsonType type, JsonCoder coder, JsonInput input) {
        try {
            switch (type) {
                case NULL:
                    return null;
                case BEGIN_OBJECT:
                    Object[] params = paramsFromObject(coder, input, paramTypes, namesToIndices);
                    return (E) initializer.invoke(null, params);
                default:
                    throw new IllegalArgumentException("Unexpected type : " + type + " when trying to parse " + initializer);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new JsonException("Error initializing", ex);
        }
    }

    public static class StaticMethodParserFactory extends JsonParserFactory {

        public StaticMethodParserFactory() {
            super(EARLY);
        }

        @Override
        public JsonParser getParserFor(Type type) {
            if (type instanceof Class) {
                Class<?> clazz = (Class) type;
                if (!((Class) type).isPrimitive()) {
                    for (Method method : clazz.getDeclaredMethods()) {
                        if (Modifier.isPublic(method.getModifiers())) {
                            StaticFactory staticFactory = method.getAnnotation(StaticFactory.class);
                            return new StaticMethodParser(method, staticFactory.value());
                        }
                    }
                }
            }
            return null;
        }
    };
}
