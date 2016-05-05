package org.jayson.parser;

import org.jayson.Jayson;
import org.jayson.JaysonException;
import org.jayson.JaysonInput;
import org.jayson.JaysonType;

/**
 *
 * @author tofar
 * @param <E>
 */
public abstract class JaysonParser<E> {

    public final E parse(Jayson coder, JaysonInput input) {
        try {
            return parse(input.next(), coder, input);
        } catch (Exception ex) {
            throw new JaysonException("Error initializing", ex);
        }
    }

    public abstract E parse(JaysonType type, Jayson coder, JaysonInput input);

}
