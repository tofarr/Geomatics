package org.jayson;

/**
 *
 * @author tofarrell
 */
public abstract class JsonConfig extends AbstractPrioritized {

    public JsonConfig(int priority) {
        super(priority);
    }

    public abstract void configure(JaysonBuilder builder);
}
