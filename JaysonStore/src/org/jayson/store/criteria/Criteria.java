package org.jayson.store.criteria;

import org.jayson.element.Element;

/**
 *
 * @author tofarr
 */
public interface Criteria {

    boolean match(Element element);
        
    void check(Element element) throws CriteriaException;
}
