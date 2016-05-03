package org.jg.io.json;

import java.lang.reflect.Type;

/**
 *
 * @author tofar
 */
public interface JsonCoder {

    Object parse(Type type, JsonReader reader) throws JsonException;

    void render(JsonWriter writer, Object value) throws JsonException;

     Object parse(Type contentType, JsonType type, JsonReader reader);
}
