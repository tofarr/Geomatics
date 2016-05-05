package org.jayson.parser;

import java.beans.Transient;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tofarrell
 */
public class SetterMap<E> {

    public final Class<E> type;
    private Map<String, Method> setters;

    public SetterMap(Class<E> type) {
        this.type = type;
        this.setters = getSetters(type);
    }

    public SetterMap(Class<E> type, Map<String, Method> setters) {
        this.type = type;
        this.setters = new HashMap<>(setters);
    }

    public boolean isEmpty() {
        return setters.isEmpty();
    }

    public Type getTypeFor(String name) {
        Method setter = setters.get(name);
        return (setter == null) ? null : setter.getGenericParameterTypes()[0];
    }

    public void apply(String name, Object value, E target) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method setter = setters.get(name);
        if (setter != null) {
            setter.invoke(target, value);
        }
    }

    static Map<String, Method> getSetters(Class type) {
        Map<String, Method> ret = new HashMap<>();
        for (Method method : type.getMethods()) {
            if (Modifier.isPublic(method.getModifiers())
                    && (!Modifier.isStatic(method.getModifiers()))
                    && (method.getParameterCount() == 1)
                    && (method.getReturnType() == void.class)
                    && (method.getAnnotation(Transient.class) == null)) {
                String name = method.getName();
                if ((name.length() > 3) && name.startsWith("set")) {
                    name = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                    ret.put(name, method);
                }
            }
        }
        return ret;
    }
}
