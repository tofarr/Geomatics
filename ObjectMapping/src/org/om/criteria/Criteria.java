package org.om.criteria;

import org.om.schema.Result;
import org.om.element.Element;
import org.om.schema.Path;

/**
 *
 * @author tofar
 */
public interface Criteria {

    boolean match(Element element);

    Result validate(Path path, Element element);
}
