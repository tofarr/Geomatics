package org.rdb.criteria.value;

import org.jayson.element.Element;
import org.jayson.element.ValElement;

/**
 *
 * @author tofarrell
 */
public class StrEndsWith extends StrContains {

    public StrEndsWith(String value) throws NullPointerException, IllegalArgumentException {
        super(value);
    }

    @Override
    public boolean match(Element element) {
        if (element instanceof ValElement) {
            return ((ValElement) element).asStr().getStr().endsWith(getValue());
        }
        return false;
    }
}
