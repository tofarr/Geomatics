package org.jsonutil.render;

import java.lang.reflect.Type;
import org.jsonutil.JsonCoder;
import org.jsonutil.JsonException;
import org.jsonutil.JsonOutput;

/**
 *
 * @author tofarrell
 */
public class ArrayRender<E> implements JsonRender<E[]> {

    public static final ArrayRender INSTANCE = new ArrayRender();

    private ArrayRender() {
    }

    @Override
    public void render(E[] values, JsonCoder coder, JsonOutput out) throws JsonException {
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
