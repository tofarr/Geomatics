package org.jayson.render;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import org.jayson.Jayson;
import org.jayson.JaysonException;
import org.jayson.JaysonOutput;

/**
 *
 * @author tofarrell
 * @param <E>
 */
public class CollectionRender<E extends Collection> implements JaysonRender<E> {

    public static final CollectionRender INSTANCE = new CollectionRender();

    private CollectionRender() {
    }

    @Override
    public void render(E values, Jayson coder, JaysonOutput out) throws JaysonException {
        out.beginArray();
        for (Object value : values) {
            coder.render(value, out);
        }
        out.endArray();
    }

    public static class CollectionRenderFactory extends JaysonRenderFactory{

        public CollectionRenderFactory(int priority){
            super(priority);
        }
        
        @Override
        public JaysonRender getRenderFor(Type type) throws JaysonException {
            if(type instanceof Class){
                Class clazz = (Class)type;
                if(Collection.class.isAssignableFrom(clazz)){
                    return INSTANCE;
                }
            }else if(type instanceof ParameterizedType){
                ParameterizedType pt = (ParameterizedType)type;
                return getRenderFor(pt.getRawType());
            }
            return null;
        }
    };
}
