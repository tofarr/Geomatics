package org.jsonutil.poly;

import org.jsonutil.AbstractPrioritized;

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
