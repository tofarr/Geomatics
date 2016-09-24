package org.roa.criteria.value;

import org.roa.element.Element;
import org.roa.element.ValElement;

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
        if(element instanceof ValElement){
            return ((ValElement)element).asStr().getStr().endsWith(getValue());
        }
        return false;
    }
}
