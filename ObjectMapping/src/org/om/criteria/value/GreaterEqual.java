package org.om.criteria.value;

import java.beans.ConstructorProperties;
import org.om.criteria.Criteria;
import org.om.element.Element;
import org.om.element.ElementType;
import org.om.element.ValueElement;

/**
 *
 * @author tofar
 */
public class GreaterEqual<C extends Comparable> implements Criteria {

    final ValueElement<C> element;

    @ConstructorProperties({"value"})
    public GreaterEqual(ValueElement<C> element) {
        this.element = element;
    }

    public ValueElement<C> getValue() {
        return element;
    }

    @Override
    public boolean match(Element element) {
        if(this.element == null){
            return (element == null); // nulls are last
        }
        if (element instanceof ValueElement) {
            ValueElement<?> v = (ValueElement) element;
            if((v.getType() == ElementType.NUMBER) && (this.element.getType() == ElementType.NUMBER)){
                return v.asNum().number >= this.element.asNum().number;
            }else{
                return v.asStr().value.compareTo(this.element.asStr().value) >= 0;
            }
        }
        return false;
    }

}
