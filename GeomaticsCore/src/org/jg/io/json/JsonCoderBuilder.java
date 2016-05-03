package org.jg.io.json;

/**
 *
 * @author tofar
 */
public interface JsonCoderBuilder {
    
    int getPriority();

    <E> JsonCoder<E> getCoder(Class<E> type) throws JsonException;
    
    
    class DefaultJsonCoderBuilder implements JsonCoderBuilder{
    
    }
}
