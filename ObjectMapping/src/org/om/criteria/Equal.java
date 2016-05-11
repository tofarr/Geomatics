package org.om.criteria;

import java.beans.ConstructorProperties;
import java.util.Objects;
import org.om.element.Element;

/**
 *
 * @author tofar
 */
public class Equal implements Criteria {

    private final Element element;

    @ConstructorProperties({"element"})
    public Equal(Element element) {
        this.element = element;
    }

    public Element getElement() {
        return element;
    }

    @Override
    public boolean match(Element element) {
        return Objects.equals(this.element, element);
    }
}
