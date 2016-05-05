package org.jayson.parser;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import org.jayson.Jayson;
import org.jayson.JsonException;
import org.jayson.JsonInput;
import org.jayson.JsonType;


/**
 *
 * @author tofarrell
 * @param <E>
 */
public class CollectionParser<E extends Collection> extends JsonParser<E> {

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
    public E parse(JsonType type, Jayson coder, JsonInput input) {
        try {
            if (type == JsonType.NULL) {
                return null;
            } else if (type != JsonType.BEGIN_ARRAY) {
                throw new JsonException("Expected array, found: " + type);
            }
            E collection = collectionType.newInstance();
            while (true) {
                type = input.next();
                if (type == JsonType.END_ARRAY) {
                    break;
                }
                Object value = coder.parse(contentType, type, input);
                collection.add(value);
            }
            return collection;
        } catch (Exception ex) {
            throw new JsonException("Error initializing", ex);
        }
    }
    
    public static class CollectionParserFactory extends JsonParserFactory{

        public CollectionParserFactory() {
            super(LATE);
        }

        @Override
        public JsonParser getParserFor(Type type) {
            if(type instanceof ParameterizedType){
                ParameterizedType pt = (ParameterizedType)type;
                Type rawType = pt.getRawType();
                if(rawType instanceof Class){
                    Class collectionType = (Class)rawType;
                    if(Collection.class.isAssignableFrom(collectionType)){
                        return new CollectionParser(collectionType, pt.getActualTypeArguments()[1]);
                    }
                }
            }
            return null;
        }
    };
}
