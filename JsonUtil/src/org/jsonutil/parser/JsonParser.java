package org.jsonutil.parser;

import org.jsonutil.JsonCoder;
import org.jsonutil.JsonException;
import org.jsonutil.JsonInput;
import org.jsonutil.JsonType;

/**
 *
 * @author tofar
 * @param <E>
 */
public abstract class JsonParser<E> {

    public final E parse(JsonCoder coder, JsonInput input) {
        try {
            return parse(input.next(), coder, input);
        } catch (Exception ex) {
            throw new JsonException("Error initializing", ex);
        }
    }

    public abstract E parse(JsonType type, JsonCoder coder, JsonInput input);

}
