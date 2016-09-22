package org.roa.criteria;

import org.ds.element.model.Element;

/**
 *
 * @author tofarr
 */
public interface Criteria {

    boolean match(Element element);
}
