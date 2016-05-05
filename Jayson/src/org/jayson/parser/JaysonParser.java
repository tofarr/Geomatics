package org.jayson.parser;

import org.jayson.Jayson;
import org.jayson.JsonException;
import org.jayson.JsonInput;
import org.jayson.JsonType;

/**
 *
 * @author tofar
 * @param <E>
 */
public abstract class JsonParser<E> {

    public final E parse(Jayson coder, JsonInput input) {
        try {
            return parse(input.next(), coder, input);
        } catch (Exception ex) {
            throw new JsonException("Error initializing", ex);
        }
    }

    public abstract E parse(JsonType type, Jayson coder, JsonInput input);

}
