package org.roa.criteria;

import org.roa.element.Element;

/**
 *
 * @author tofarr
 */
public interface Criteria {

    boolean match(Element element);
}
