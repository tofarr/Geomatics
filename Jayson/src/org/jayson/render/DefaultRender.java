package org.jayson.render;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.jayson.Jayson;
import org.jayson.JsonException;
import org.jayson.JsonOutput;

/**
 *
 * @author tofarrell
 * @param <E>
 */
public class DefaultRender<E> implements JsonRender<E> {

    private final Field[] fields;
    private final Map<String, Method> getters;

    private DefaultRender(Field[] fields, Map<String, Method> getters) {
        this.fields = fields;
        this.getters = getters;
    }

    @Override
    public void render(E value, Jayson coder, JsonOutput out) {
        out.beginObject();
        renderContent(value, coder, out);
        out.endObject();
    }

    public void renderContent(E value, Jayson coder, JsonOutput out) {
        for (Field field : fields) {
            try {
                Object fieldValue = field.get(value);
                if (fieldValue != null) {
                    out.name(field.getName());
                    coder.render(fieldValue, out);
                }
            } catch (IllegalArgumentException | IllegalAccessException | JsonException | NullPointerException | IllegalStateException ex) {
                throw new JsonException("Error rendering", ex);
            }
        }
        for (Entry<String, Method> entry : getters.entrySet()) {
            try {
                Object gotValue = entry.getValue().invoke(value);
                if (gotValue != null) {
                    out.name(entry.getKey());
                    coder.render(gotValue, out);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | JsonException | NullPointerException | IllegalStateException ex) {
                throw new JsonException("Error rendering", ex);
            }
        }
    }

    public static class DefaultRenderFactory extends JsonRenderFactory {

        public DefaultRenderFactory() {
            super(EARLY);
        }

        @Override
        public JsonRender getRenderFor(Type type) throws JsonException {
            if (type instanceof Class) {
                Class clazz = (Class) type;
                if (!(clazz.isPrimitive() || clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers()))) {
                    List<Field> fields = new ArrayList<>();
                    for (Field field : clazz.getFields()) {
                        if (!Modifier.isTransient(field.getModifiers())) {
                            fields.add(field);
                        }
                    }
                    Map<String, Method> getters = new HashMap<>();
                    for (Method method : clazz.getMethods()) {
                        String name = method.getName();
                        if ((method.getParameterCount() == 0)
                                && name.startsWith("get")
                                && (method.getReturnType() != null)
                                && (method.getAnnotation(Transient.class) == null)) {
                            name = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                            for (Iterator<Field> iter = fields.iterator(); iter.hasNext();) {
                                Field field = iter.next();
                                if (field.getName().equals(name)) { // getters take priority over fields
                                    iter.remove();
                                    break;
                                }
                            }
                            getters.put(name, method);
                        }
                    }
                    return new DefaultRender(fields.toArray(new Field[fields.size()]), getters);
                }
            }
            return null;
        }

    };
}
