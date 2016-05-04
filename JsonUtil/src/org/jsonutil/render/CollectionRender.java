package org.jsonutil.render;

import java.lang.reflect.Type;
import java.util.Collection;
import org.jsonutil.JsonCoder;
import org.jsonutil.JsonException;
import org.jsonutil.JsonOutput;

/**
 *
 * @author tofarrell
 * @param <E>
 */
public class CollectionRender<E extends Collection> implements JsonRender<E> {

    public static final CollectionRender INSTANCE = new CollectionRender();

    private CollectionRender() {
    }

    @Override
    public void render(E values, JsonCoder coder, JsonOutput out) throws JsonException {
        out.beginArray();
        for (Object value : values) {
            coder.render(value, out);
        }
        out.endArray();
    }

    public static class CollectionRenderFactory extends JsonRenderFactory{

        public CollectionRenderFactory() {
            super(LATE);
        }
        
        @Override
        public JsonRender getRenderFor(Type type) throws JsonException {
            if(type instanceof Class){
                Class clazz = (Class)type;
                if(Collection.class.isAssignableFrom(clazz)){
                    return INSTANCE;
                }
            }
            return null;
        }
    };
}
