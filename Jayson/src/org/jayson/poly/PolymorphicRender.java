package org.jayson.poly;

import java.lang.reflect.Type;
import org.jayson.JsonBuffer;
import org.jayson.Jayson;
import org.jayson.JsonException;
import org.jayson.JsonInput;
import org.jayson.JsonOutput;
import org.jayson.JsonType;
import org.jayson.render.JsonRender;
import org.jayson.render.JsonRenderFactory;

/**
 *
 * @author tofarrell
 * @param <E>
 */
public class PolymorphicRender<E> implements JsonRender<E> {

    private final ClassMap<E> classMap;

    public PolymorphicRender(ClassMap<E> classMap) throws NullPointerException {
        this.classMap = new ClassMap<>(classMap);
    }
    
    public ClassMap<E> getClassMap(){
        return new ClassMap<>(classMap);
    }

    @Override
    public void render(E value, Jayson coder, JsonOutput out) throws JsonException {
        Class clazz = value.getClass();
        String name = classMap.getName(clazz);
        JsonBuffer buffer = new JsonBuffer();
        coder.render(value, clazz, buffer);
        JsonInput input = buffer.getInput();
        JsonType type = input.next();
        if(type != JsonType.BEGIN_OBJECT){
            throw new JsonException("Expected BEGIN_OBJECT, found " + type);
        }
        out.beginObject().name("$type").str(name);
        out.copyRemaining(input);
    }    
    
    public static class PolymorphicRenderFactory extends JsonRenderFactory{

        private final PolymorphicMap map;
        
        public PolymorphicRenderFactory(PolymorphicMap map) throws NullPointerException {
            super(EARLY);
            if(map == null){
                throw new NullPointerException();
            }
            this.map = map;
        }

        @Override
        public JsonRender getRenderFor(Type type) {
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
