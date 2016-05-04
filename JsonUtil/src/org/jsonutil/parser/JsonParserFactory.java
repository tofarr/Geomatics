package org.jsonutil.parser;

import java.lang.reflect.Type;
import org.jsonutil.AbstractPrioritized;

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
