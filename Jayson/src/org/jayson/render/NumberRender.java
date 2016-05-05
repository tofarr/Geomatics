package org.jayson.render;

import java.lang.reflect.Type;
import org.jayson.Jayson;
import org.jayson.JsonException;
import org.jayson.JsonOutput;

/**
 *
 * @author tofarrell
 */
public class NumberRender implements JsonRender<Number> {

    public static final NumberRender INSTANCE = new NumberRender();
    
    private NumberRender(){
    }
    
    @Override
    public void render(Number value, Jayson coder, JsonOutput out) throws JsonException {
        out.num(value.doubleValue());        
    }
    
    public static class NumberRenderFactory extends JsonRenderFactory{

        public NumberRenderFactory() {
            super(LATE);
        }
        
        @Override
        public JsonRender getRenderFor(Type type) throws JsonException {
            if(type instanceof Class){
                Class clazz = (Class)type;
                if(clazz.isPrimitive()){
                    if(clazz == byte.class
                            || clazz == double.class 
                            || clazz == float.class 
                            || clazz == int.class 
                            || clazz == long.class
                            || clazz == short.class){
                        return INSTANCE;
                    }
                }else if(Number.class.isAssignableFrom(clazz)){
                    return INSTANCE;
                }
            }
            return null;
        }
    };
}
