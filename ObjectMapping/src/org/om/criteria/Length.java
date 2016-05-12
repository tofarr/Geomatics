package org.om.criteria;

import java.beans.ConstructorProperties;
import org.om.element.ArrElement;
import org.om.element.Element;
import org.om.element.NumElement;
import org.om.element.ValElement;

/**
 *
 * @author tofarrell
 */
public class Length implements Criteria {

    private final Criteria criteria;

    @ConstructorProperties({"criteria"})
    public Length(Criteria criteria) {
        if (criteria == null) {
            throw new NullPointerException("criteria must not be null!");
        }
        this.criteria = criteria;
    }

    @Override
    public boolean match(Element element) {
        if (element instanceof ValElement) {
            NumElement num = NumElement.valueOf(((ValElement)element).asStr().str.length());
            return criteria.match(num);
        }else if(element instanceof ArrElement){
            NumElement num = NumElement.valueOf(((ArrElement)element).size());
            return criteria.match(num);
        }
        return false;
    }

}
