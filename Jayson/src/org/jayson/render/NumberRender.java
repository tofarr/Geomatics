package org.jayson.render;

import java.lang.reflect.Type;
import org.jayson.Jayson;
import org.jayson.JaysonException;
import org.jayson.JaysonOutput;

/**
 *
 * @author tofarrell
 */
public class NumberRender implements JaysonRender<Number> {

    public static final NumberRender INSTANCE = new NumberRender();
    
    private NumberRender(){
    }
    
    @Override
    public void render(Number value, Jayson coder, JaysonOutput out) throws JaysonException {
        out.num(value.doubleValue());        
    }
    
    public static class NumberRenderFactory extends JaysonRenderFactory{

        public NumberRenderFactory(int priority){
            super(priority);
        }
        
        @Override
        public JaysonRender getRenderFor(Type type) throws JaysonException {
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
