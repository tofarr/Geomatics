package org.jg.io.json;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tofar
 * @param <E>
 */
public abstract class JsonParser<E> {

    public final E parse(JsonCoder coder, JsonReader reader) {
        try {
            return parse(reader.next(), coder, reader);
        } catch (Exception ex) {
            throw new JsonException("Error initializing", ex);
        }
    }
    
    public abstract E parse(JsonType type, JsonCoder coder, JsonReader reader);

    public static class ConstructorParser<E> extends JsonParser<E> {

        private final Constructor<E> constructor;
        private final Type[] paramTypes;
        private final Map<String, Integer> namesToIndices;

        public ConstructorParser(Constructor<E> constructor, String... propertyNames) {
            this.constructor = constructor;
            this.paramTypes = constructor.getGenericParameterTypes();
            if(propertyNames.length != paramTypes.length){
                throw new IllegalArgumentException("Wrong number of propertyNames : "+constructor);
            }
            Map<String, Integer> nameToIndexMap = new HashMap<>();
            for(int p = propertyNames.length; p-- > 0;){
                nameToIndexMap.put(propertyNames[p], p);
            }
            this.namesToIndices = Collections.unmodifiableMap(nameToIndexMap);
        }
        
        @Override
        public E parse(JsonType type, JsonCoder coder, JsonReader reader) {
            try {
                switch (type) {
                    case NULL:
                        return null;
                    case BEGIN_OBJECT:
                        Object[] params = paramsFromObject(coder, reader, paramTypes, namesToIndices);
                        return constructor.newInstance(params);
                    default:
                        throw new IllegalArgumentException("Unexpected type : "+type+" when trying to parse "+constructor);
                }
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new JsonException("Error initializing", ex);
            }
        }
    }
    
    
    public static class StaticMethodParser<E> extends JsonParser<E> {

        private final Method initializer;
        private final Type[] paramTypes;
        private final Map<String, Integer> namesToIndices;

        public StaticMethodParser(Method initializer, String... propertyNames) {
            if(!Modifier.isStatic(initializer.getModifiers())){
                throw new IllegalArgumentException("Method is not static "+initializer);
            }
            this.initializer = initializer;
            this.paramTypes = initializer.getGenericParameterTypes();
            if(propertyNames.length != paramTypes.length){
                throw new IllegalArgumentException("Wrong number of propertyNames : "+initializer);
            }
            Map<String, Integer> nameToIndexMap = new HashMap<>();
            for(int p = propertyNames.length; p-- > 0;){
                nameToIndexMap.put(propertyNames[p], p);
            }
            this.namesToIndices = Collections.unmodifiableMap(nameToIndexMap);
        }
        
        @Override
        public E parse(JsonType type, JsonCoder coder, JsonReader reader) {
            try{
                switch (type) {
                    case NULL:
                        return null;
                    case BEGIN_OBJECT:
                        Object[] params = paramsFromObject(coder, reader, paramTypes, namesToIndices);
                        return (E)initializer.invoke(null, params);
                    default:
                        throw new IllegalArgumentException("Unexpected type : "+type+" when trying to parse "+initializer);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new JsonException("Error initializing", ex);
            }
        }
    }
    
    public static class CollectionParser<E extends Collection> extends JsonParser<E> {

        private final Class<E> collectionType;
        private final Type contentType;

        public CollectionParser(Class<E> collectionType, Type contentType) {
            if(collectionType == null || contentType == null){
                throw new NullPointerException();
            }
            this.collectionType = collectionType;
            this.contentType = contentType;
        }
        
        @Override
        public E parse(JsonType type, JsonCoder coder, JsonReader reader) {
            try{
                if(type == JsonType.NULL){
                    return null;
                }else if(type != JsonType.BEGIN_ARRAY){
                    throw new JsonException("Expected array, found: "+type);
                }
                E collection = collectionType.newInstance();
                while(true){
                    type = reader.next();
                    if(type == JsonType.END_ARRAY){
                        break;
                    }
                    Object value = coder.parse(contentType, type, reader);
                    collection.add(value);
                }
                return collection;
            } catch (Exception ex) {
                throw new JsonException("Error initializing", ex);
            }
        }
    }
    
    
    public static class ArrayParser<E> extends JsonParser<E> {

        private final CollectionParser parser;

        public ArrayParser(CollectionParser parser) {
            this.parser = parser;
        }

        @Override
        public E parse(JsonType type, JsonCoder coder, JsonReader reader) {
            Collection collection = parser.parse(type, coder, reader);
            Object[] array = (Object[])Array.newInstance((Class)parser.contentType, collection.size());
            return (E)collection.toArray(array);
        }
    }
    
    public static class StringParser extends JsonParser<String>{

        @Override
        public String parse(JsonType type, JsonCoder coder, JsonReader reader) {
            switch(type){
                case BOOLEAN:
                    return Boolean.toString(reader.bool());
                case NULL:
                    return null;
                case NUMBER:
                    return Double.toString(reader.num());
                case STRING:
                    return reader.str();
                default:
                    throw new JsonException("Expected STRING, found "+type);
            }
        }
        
    }
    
    public static class BooleanParser extends JsonParser<Boolean>{
        
        @Override
        public Boolean parse(JsonType type, JsonCoder coder, JsonReader reader) {
            switch(type){
                case BOOLEAN:
                    return reader.bool();
                case NULL:
                    return null;
                case NUMBER:
                    return reader.num() != 0;
                case STRING:
                    return !("false".equalsIgnoreCase(reader.str()) || reader.str().isEmpty() || "0".equals(reader.str()));
                default:
                    throw new JsonException("Expected STRING, found "+type);
            }
        }
    }
    
    public static class NumberParser extends JsonParser<Number>{
        
    }
    
    

    static Object[] paramsFromObject(JsonCoder coder, JsonReader reader, Type[] paramTypes, Map<String,Integer> namesToIndices){
        Object[] params = new Object[paramTypes.length];
        while(reader.next() == JsonType.NAME){
            String key = reader.str();
            Integer index = namesToIndices.get(key);
            if(index != null){
                Type type = paramTypes[index];
                params[index] = coder.parse(type, reader);
            }else{
                skip(reader); // skip not found
            }
        }
        assignPrimatives(paramTypes, params);
        return params;
    }

    static void skip(JsonReader reader){
        int count = 0;
        do{
            JsonType type = reader.next();
            switch(type){
                case BEGIN_ARRAY:
                case BEGIN_OBJECT:
                    count++;
                    break;
                case END_ARRAY:
                case END_OBJECT:
                    count--;
                    break;
            }
        }while(count > 0);
    }
    
    static void assignPrimatives(Type[] paramTypes, Object[] params) {
        for (int i = params.length; i-- > 0;) {
            if (params[i] != null) {
                continue;
            }
            Type type = paramTypes[i];
            if (type == boolean.class) {
                params[i] = false;
            } else if (type == byte.class) {
                params[i] = (byte) 0;
            } else if (type == char.class) {
                params[i] = (char) 0;
            } else if (type == double.class) {
                params[i] = 0.0;
            } else if (type == float.class) {
                params[i] = 0.0f;
            } else if (type == int.class) {
                params[i] = 0;
            } else if (type == long.class) {
                params[i] = 0L;
            } else if (type == short.class) {
            }
            params[i] = (short) 0;
        }
    }
}
