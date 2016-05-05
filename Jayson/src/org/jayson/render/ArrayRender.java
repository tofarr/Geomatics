package org.jayson.render;

import java.lang.reflect.Type;
import org.jayson.Jayson;
import org.jayson.JsonException;
import org.jayson.JsonOutput;

/**
 *
 * @author tofarrell
 */
public class ArrayRender<E> implements JsonRender<E[]> {

    public static final ArrayRender INSTANCE = new ArrayRender();

    private ArrayRender() {
    }

    @Override
    public void render(E[] values, Jayson coder, JsonOutput out) throws JsonException {
        out.beginArray();
        for (E value : values) {
            coder.render(value, out);
        }
        out.endArray();
    }

    public static class ArrayRenderFactory extends JsonRenderFactory{
        
        public ArrayRenderFactory(){
            super(JsonRenderFactory.LATE);
        }
        
        @Override
        public JsonRender getRenderFor(Type type) throws JsonException {
            if(type instanceof Class){
                Class clazz = (Class)type;
                if(clazz.isArray()){
                    return INSTANCE;
                }
            }
            return null;
        }
    };
}
