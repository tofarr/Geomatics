package org.jayson.parser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
public class StaticMethodParser<E> extends JaysonParser<E> {

    private final Method initializer;
    private final Type[] paramTypes;
    private final Map<String, Integer> namesToIndices;
    private final SetterMap setterMap;

    public StaticMethodParser(Method initializer, SetterMap setterMap, String... propertyNames) {
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
        this.setterMap = setterMap;
    }

    @Override
    public E parse(JaysonType type, Jayson coder, JaysonInput input) {
        try {
            switch (type) {
                case NULL:
                    return null;
                case BEGIN_OBJECT:
                    Object[] params = (paramTypes.length != 0) ? new Object[paramTypes.length] : ConstructorParser.EMPTY;
                    Map<String,Object> setterValues = (setterMap == null) ? null : new HashMap<>();
                    ConstructorParser.parseParams(coder, input, paramTypes, namesToIndices, params, setterMap, setterValues);
                    E ret = (E)initializer.invoke(null, params);
                    if(setterValues != null){
                        for(Entry<String,Object> entry : setterValues.entrySet()){
                            setterMap.apply(entry.getKey(), entry.getValue(), ret);
                        }
                    }
                    return ret;
                default:
                    throw new IllegalArgumentException("Unexpected type : " + type + " when trying to parse " + initializer);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new JaysonException("Error initializing", ex);
        }
    }

    public static class StaticMethodParserFactory extends JaysonParserFactory {

        public StaticMethodParserFactory(int priority) {
            super(priority);
        }

        @Override
        public JaysonParser getParserFor(Type type) {
            if (type instanceof Class) {
                Class<?> clazz = (Class) type;
                if (!((Class) type).isPrimitive()) {
                    for (Method method : clazz.getDeclaredMethods()) {
                        if (Modifier.isPublic(method.getModifiers())) {
                            StaticFactory staticFactory = method.getAnnotation(StaticFactory.class);
                            if(staticFactory != null){
                                SetterMap setterMap = new SetterMap(clazz);
                                if(setterMap.isEmpty()){
                                    setterMap = null;
                                }
                                return new StaticMethodParser(method, setterMap, staticFactory.value());
                            }
                        }
                    }
                }
            }
            return null;
        }
    };
}
