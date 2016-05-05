package org.jayson.poly;

import java.lang.reflect.Type;
import org.jayson.JaysonBuffer;
import org.jayson.Jayson;
import org.jayson.JaysonException;
import org.jayson.JaysonInput;
import org.jayson.JaysonType;
import org.jayson.parser.JaysonParser;
import org.jayson.parser.JaysonParserFactory;

/**
 *
 * @author tofarrell
 */
public class PolymorphicParser<E> extends JaysonParser<E> {

    private final ClassMap<E> classMap;

    public PolymorphicParser(ClassMap<E> classMap) throws NullPointerException {
        this.classMap = new ClassMap<>(classMap);
    }
    
    public ClassMap<E> getClassMap(){
        return new ClassMap<>(classMap);
    }

    @Override
    public E parse(JaysonType type, Jayson coder, JaysonInput input) throws JaysonException {
        if (type != JaysonType.BEGIN_OBJECT) {
            throw new JaysonException("Expected BEGIN_OBJECT found : " + type);
        }

        // Create a buffer containing the whole json object
        JaysonBuffer buffer = new JaysonBuffer();
        buffer.beginObject();
        buffer.copyRemaining(input);

        String name = buffer.findFirstStr("$type", 1);
        Class<? extends E> implClass = classMap.getImplClass(name);
        if (implClass == null) {
            throw new JaysonException("Unknown type " + name);
        }
        return coder.parse(implClass, buffer.getInput());
    }
    
    public static class PolymorphicParserFactory extends JaysonParserFactory{

        private final PolymorphicMap map;
        
        public PolymorphicParserFactory(int priority, PolymorphicMap map) throws NullPointerException {
            super(priority);
            if(map == null){
                throw new NullPointerException();
            }
            this.map = map;
        }

        @Override
        public JaysonParser getParserFor(Type type) {
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
