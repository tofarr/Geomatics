package org.jayson.parser;

import java.lang.reflect.Type;
import org.jayson.AbstractPrioritized;

/**
 *
 * @author tofarrell
 */
public abstract class JsonParserFactory extends AbstractPrioritized {

    public JsonParserFactory(int priority) {
        super(priority);
    }

    public abstract JsonParser getParserFor(Type type);
}
