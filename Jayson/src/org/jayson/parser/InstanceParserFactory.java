package org.jayson.parser;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 *
 * @author tofar
 */
public class InstanceParserFactory<E> extends JaysonParserFactory {

    public final Class<E> type;
    public final JaysonParser<E> parser;

    public InstanceParserFactory(Class<E> type, JaysonParser<E> parser, int priority) {
        super(priority);
        this.type = type;
        this.parser = parser;
    }

    @Override
    public JaysonParser getParserFor(Type type) {
        if(type instanceof Class){
            Class clazz = (Class)type;
            if(this.type.isAssignableFrom(clazz)){
                return parser;
            }
        }else if(type instanceof ParameterizedType){
            ParameterizedType pt = (ParameterizedType)type;
            return getParserFor(pt.getRawType());  
        }
        return null;
    }
}
