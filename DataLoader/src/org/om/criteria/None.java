package org.om.criteria;

import org.jayson.parser.StaticFactory;
import org.om.element.Element;

/**
 *
 * @author tofarrell
 */
public class None implements Criteria {

    public static final None INSTANCE = new None();

    private None() {
    }

    @StaticFactory({})
    public static None getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean match(Element element) {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == INSTANCE;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }

}
