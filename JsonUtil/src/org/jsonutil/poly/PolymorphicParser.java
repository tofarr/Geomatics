package org.jsonutil.poly;

import java.lang.reflect.Type;
import org.jsonutil.JsonBuffer;
import org.jsonutil.JsonCoder;
import org.jsonutil.JsonException;
import org.jsonutil.JsonInput;
import org.jsonutil.JsonType;
import org.jsonutil.parser.JsonParser;
import org.jsonutil.parser.JsonParserFactory;

/**
 *
 * @author tofarrell
 */
public class PolymorphicParser<E> extends JsonParser<E> {

    private final ClassMap<E> classMap;

    public PolymorphicParser(ClassMap<E> classMap) throws NullPointerException {
        this.classMap = new ClassMap<>(classMap);
    }
    
    public ClassMap<E> getClassMap(){
        return new ClassMap<>(classMap);
    }

    @Override
    public E parse(JsonType type, JsonCoder coder, JsonInput input) throws JsonException {
        if (type != JsonType.BEGIN_OBJECT) {
            throw new JsonException("Expected BEGIN_OBJECT found : " + type);
        }

        // Create a buffer containing the whole json object
        JsonBuffer buffer = new JsonBuffer();
        buffer.beginObject();
        buffer.copyRemaining(input);

        String name = buffer.findFirstStr("$type", 1);
        Class<? extends E> implClass = classMap.getImplClass(name);
        if (implClass == null) {
            throw new JsonException("Unknown type " + name);
        }
        return coder.parse(implClass, buffer.getInput());
    }
    
    public static class PolymorphicParserFactory extends JsonParserFactory{

        private final PolymorphicMap map;
        
        public PolymorphicParserFactory(PolymorphicMap map) throws NullPointerException {
            super(EARLY);
            if(map == null){
                throw new NullPointerException();
            }
            this.map = map;
        }

        @Override
        public JsonParser getParserFor(Type type) {
            if(type instanceof Class){
                Class clazz = (Class)type;
                ClassMap classMap = map.getClassMap(clazz);
                if(classMap != null){
                    return new PolymorphicParser(classMap);
                }
            }
            return null;
        }
    }
}
