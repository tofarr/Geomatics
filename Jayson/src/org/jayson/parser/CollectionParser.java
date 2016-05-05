package org.jayson.parser;


import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.jayson.Jayson;
import org.jayson.JaysonException;
import org.jayson.JaysonInput;
import org.jayson.JaysonType;


/**
 *
 * @author tofarrell
 * @param <E>
 */
public class CollectionParser<E extends Collection> extends JaysonParser<E> {

    final Class<E> collectionType;
    final Type contentType;

    public CollectionParser(Class<E> collectionType, Type contentType) {
        if (collectionType == null || contentType == null) {
            throw new NullPointerException();
        }
        this.collectionType = collectionType;
        this.contentType = contentType;
    }

    @Override
    public E parse(JaysonType type, Jayson coder, JaysonInput input) {
        try {
            if (type == JaysonType.NULL) {
                return null;
            } else if (type != JaysonType.BEGIN_ARRAY) {
                throw new JaysonException("Expected array, found: " + type);
            }
            E collection = collectionType.newInstance();
            while (true) {
                type = input.next();
                if (type == JaysonType.END_ARRAY) {
                    break;
                }
                Object value = coder.parse(contentType, type, input);
                collection.add(value);
            }
            return collection;
        } catch (Exception ex) {
            throw new JaysonException("Error initializing", ex);
        }
    }
    
    public static class CollectionParserFactory extends JaysonParserFactory{

        public CollectionParserFactory(int priority) {
            super(priority);
        }

        @Override
        public JaysonParser getParserFor(Type type) {
            if(type instanceof ParameterizedType){
                ParameterizedType pt = (ParameterizedType)type;
                Type rawType = pt.getRawType();
                if(rawType instanceof Class){
                    Class collectionType = (Class)rawType;
                    if(Collection.class.isAssignableFrom(collectionType)){
                        collectionType = getImpl(collectionType);
                        return new CollectionParser(collectionType, pt.getActualTypeArguments()[0]);
                    }
                }
            }
            return null;
        }
        
        public static Class getImpl(Class clazz){
            if(clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())){
                if(Set.class.isAssignableFrom(clazz)){
                    clazz = HashSet.class;
                }else{
                    clazz = ArrayList.class;
                }
            }
            return clazz;
        }
    };
}
