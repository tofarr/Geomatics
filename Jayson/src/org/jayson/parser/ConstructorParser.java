package org.jayson.parser;

import java.beans.ConstructorProperties;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.jayson.Jayson;
import org.jayson.JaysonException;
import org.jayson.JaysonInput;
import org.jayson.JaysonType;

/**
 *
 * @author tofarrell
 * @param <E>
 */
public class ConstructorParser<E> extends JaysonParser<E> {

    static final Object[] EMPTY = new Object[0];
    private final Constructor<E> constructor;
    private final Type[] paramTypes;
    private final Map<String, Integer> namesToIndices;
    private final SetterMap setterMap;

    public ConstructorParser(Constructor<E> constructor, SetterMap setterMap, String... propertyNames) throws NullPointerException {
        this.constructor = constructor;
        this.setterMap = setterMap;
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
    public E parse(JaysonType type, Jayson coder, JaysonInput input) {
        try {
            switch (type) {
                case NULL:
                    return null;
                case BEGIN_OBJECT:
                    Object[] params = (paramTypes.length != 0) ? new Object[paramTypes.length] : EMPTY;
                    Map<String,Object> setterValues = (setterMap == null) ? null : new HashMap<>();
                    parseParams(coder, input, paramTypes, namesToIndices, params, setterMap, setterValues);
                    E ret = constructor.newInstance(params);
                    if(setterValues != null){
                        for(Entry<String,Object> entry : setterValues.entrySet()){
                            setterMap.apply(entry.getKey(), entry.getValue(), ret);
                        }
                    }
                    return ret;
                default:
                    throw new IllegalArgumentException("Unexpected type : " + type + " when trying to parse " + constructor);
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new JaysonException("Error initializing", ex);
        }
    }

    public static class ConstructorParserFactory extends JaysonParserFactory {

        public ConstructorParserFactory(int priority) {
            super(priority);
        }

        @Override
        public JaysonParser getParserFor(Type type) {
            if (type instanceof Class) {
                Class<?> clazz = (Class) type;
                if (!clazz.isPrimitive()) {
                    for (Constructor<?> constructor : clazz.getConstructors()) {
                        if (Modifier.isPublic(constructor.getModifiers())) {
                            ConstructorProperties constructorProperties = constructor.getAnnotation(ConstructorProperties.class);
                            if(constructorProperties != null){
                                SetterMap setterMap = new SetterMap(clazz);
                                if(setterMap.isEmpty()){
                                    setterMap = null;
                                }
                                return new ConstructorParser(constructor, setterMap, constructorProperties.value());
                            }
                        }
                    }
                    for (Constructor<?> constructor : clazz.getConstructors()) {
                        if (Modifier.isPublic(constructor.getModifiers())) {
                            if(constructor.getParameterCount() == 0){
                                SetterMap setterMap = new SetterMap(clazz);
                                if(setterMap.isEmpty()){
                                    setterMap = null;
                                }
                                return new ConstructorParser(constructor, setterMap);
                            }
                        }
                    }
                }
            }
            return null;
        }

    };

    static void parseParams(Jayson coder, JaysonInput input,
            Type[] paramTypes, Map<String, Integer> namesToIndices, Object[] params,
            SetterMap setterMap, Map<String,Object> setterValues) {
        while (input.next() == JaysonType.NAME) {
            String key = input.str();
            Integer index = namesToIndices.get(key);
            if (index != null) {
                Type type = paramTypes[index];
                params[index] = coder.parse(type, input);
                continue;
            } else if(setterMap != null){
                Type type = setterMap.getTypeFor(key);
                if(type != null){
                    Object value = coder.parse(type, input);
                    setterValues.put(key, value);
                    continue;
                }
            }
            input.skip(); // skip not found
        }
        assignPrimatives(paramTypes, params);
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
                params[i] = (short) 0;
            }
        }
    }
}
