package org.jayson.poly;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author tofarrell
 */
public class DefaultPolymorphicConfig extends PolymorphicConfig {

    public DefaultPolymorphicConfig() {
        super(MED);
    }

    @Override
    public void configure(PolymorphicMapBuilder builder) {
        builder.getClassMap(Collection.class).add(ArrayList.class);
        builder.getClassMap(List.class).add(ArrayList.class);
        builder.getClassMap(Set.class).add(HashSet.class);
    }

}
