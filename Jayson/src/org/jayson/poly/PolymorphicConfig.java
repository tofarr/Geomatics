package org.jayson.poly;

import org.jayson.AbstractPrioritized;

/**
 *
 * @author tofarrell
 */
public abstract class PolymorphicConfig extends AbstractPrioritized {

    public PolymorphicConfig(int priority) {
        super(priority);
    }

    public abstract void configure(PolymorphicMapBuilder builder);
}
