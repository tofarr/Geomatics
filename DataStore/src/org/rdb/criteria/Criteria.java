
package org.rdb.criteria;

import org.jayson.element.Element;

/**
 *
 * @author tofarr
 */
public interface Criteria {
    
    boolean match(Element element);
}
