package org.jayson.parser;

import java.lang.reflect.Type;
import org.jayson.AbstractPrioritized;

/**
 *
 * @author tofarrell
 */
public abstract class JaysonParserFactory extends AbstractPrioritized {

    public JaysonParserFactory(int priority) {
        super(priority);
    }

    public abstract JaysonParser getParserFor(Type type);
}
