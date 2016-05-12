package org.jayson.render;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 *
 * @author tofar
 */
public class InstanceRenderFactory<E> extends JaysonRenderFactory {

    public final Class<E> type;
    public final JaysonRender<E> render;

    public InstanceRenderFactory(Class<E> type, JaysonRender<E> render, int priority) {
        super(priority);
        this.type = type;
        this.render = render;
    }

    @Override
    public JaysonRender getRenderFor(Type type) {
        if (type instanceof Class) {
            Class clazz = (Class) type;
            if (this.type.isAssignableFrom(clazz)) {
                return render;
            }
        }else if(type instanceof ParameterizedType){
            ParameterizedType pt = (ParameterizedType)type;
            return getRenderFor(pt.getRawType());  
        }
        return null;
    }
}
