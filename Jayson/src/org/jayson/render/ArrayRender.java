package org.jayson.render;

import java.lang.reflect.Type;
import org.jayson.Jayson;
import org.jayson.JaysonException;
import org.jayson.JaysonOutput;

/**
 *
 * @author tofarrell
 */
public class ArrayRender<E> implements JaysonRender<E[]> {

    public static final ArrayRender INSTANCE = new ArrayRender();

    private ArrayRender() {
    }

    @Override
    public void render(E[] values, Jayson coder, JaysonOutput out) throws JaysonException {
        out.beginArray();
        for (E value : values) {
            coder.render(value, out);
        }
        out.endArray();
    }

    public static class ArrayRenderFactory extends JaysonRenderFactory{
        
        public ArrayRenderFactory(int priority){
            super(priority);
        }
        
        @Override
        public JaysonRender getRenderFor(Type type) throws JaysonException {
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
