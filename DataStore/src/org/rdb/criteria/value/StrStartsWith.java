package org.rdb.criteria.value;

import org.jayson.element.Element;
import org.jayson.element.ValElement;

/**
 *
 * @author tofarrell
 */
public class StrStartsWith extends StrContains {

    public StrStartsWith(String value) throws NullPointerException, IllegalArgumentException {
        super(value);
    }

    @Override
    public boolean match(Element element) {
        if (element instanceof ValElement) {
            return ((ValElement) element).asStr().getStr().startsWith(getValue());
        }
        return false;
    }

}
