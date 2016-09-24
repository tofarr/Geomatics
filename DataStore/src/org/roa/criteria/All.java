package org.roa.criteria;

import org.roa.element.Element;
import org.jayson.parser.StaticFactory;

/**
 *
 * @author tofarrell
 */
public class All implements Criteria {

    public static final All INSTANCE = new All();

    private All() {
    }

    @StaticFactory({})
    public static All getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean match(Element element) {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == INSTANCE;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }

}
