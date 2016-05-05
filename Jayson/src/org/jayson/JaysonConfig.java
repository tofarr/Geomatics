package org.jayson;

/**
 *
 * @author tofarrell
 */
public abstract class JaysonConfig extends AbstractPrioritized {

    public JaysonConfig(int priority) {
        super(priority);
    }

    public abstract void configure(JaysonBuilder builder);
}
