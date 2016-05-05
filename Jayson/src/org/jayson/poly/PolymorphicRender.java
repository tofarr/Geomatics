package org.jayson.poly;

import java.lang.reflect.Type;
import org.jayson.JaysonBuffer;
import org.jayson.Jayson;
import org.jayson.JaysonException;
import org.jayson.JaysonInput;
import org.jayson.JaysonOutput;
import org.jayson.JaysonType;
import org.jayson.render.JaysonRenderFactory;
import org.jayson.render.JaysonRender;

/**
 *
 * @author tofarrell
 * @param <E>
 */
public class PolymorphicRender<E> implements JaysonRender<E> {

    private final ClassMap<E> classMap;

    public PolymorphicRender(ClassMap<E> classMap) throws NullPointerException {
        this.classMap = new ClassMap<>(classMap);
    }
    
    public ClassMap<E> getClassMap(){
        return new ClassMap<>(classMap);
    }

    @Override
    public void render(E value, Jayson coder, JaysonOutput out) throws JaysonException {
        Class clazz = value.getClass();
        String name = classMap.getName(clazz);
        JaysonBuffer buffer = new JaysonBuffer();
        coder.render(value, clazz, buffer);
        JaysonInput input = buffer.getInput();
        JaysonType type = input.next();
        if(type != JaysonType.BEGIN_OBJECT){
            throw new JaysonException("Expected BEGIN_OBJECT, found " + type);
        }
        out.beginObject().name("$type").str(name);
        out.copyRemaining(input);
    }    
    
    public static class PolymorphicRenderFactory extends JaysonRenderFactory{

        private final PolymorphicMap map;
        
        public PolymorphicRenderFactory(int priority, PolymorphicMap map) throws NullPointerException {
            super(priority);
            if(map == null){
                throw new NullPointerException();
            }
            this.map = map;
        }

        @Override
        public JaysonRender getRenderFor(Type type) {
            if(type instanceof Class){
                Class clazz = (Class)type;
                ClassMap classMap = map.getClassMap(clazz);
                if(classMap != null){
                    return new PolymorphicRender(classMap);
                }
            }
            return null;
        }
    }
}
