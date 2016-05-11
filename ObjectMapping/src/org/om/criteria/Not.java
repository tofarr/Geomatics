package org.om.criteria;

import java.beans.ConstructorProperties;
import org.om.element.Element;

/**
 *
 * @author tofar
 */
public class Not implements Criteria {

    private final Criteria criteria;

    @ConstructorProperties({"criteria"})
    public Not(Criteria criteria) throws NullPointerException {
        if (criteria == null) {
            throw new NullPointerException("criteria must not be null");
        }
        this.criteria = criteria;
    }

    public Criteria getCriteria() {
        return criteria;
    }

    @Override
    public boolean match(Element element) {
        return !criteria.match(element);
    }
}
