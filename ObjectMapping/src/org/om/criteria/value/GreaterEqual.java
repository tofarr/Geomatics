package org.om.criteria.value;

import java.beans.ConstructorProperties;
import org.om.criteria.Criteria;
import org.om.element.Element;
import org.om.element.ElementType;
import org.om.element.ValElement;

/**
 *
 * @author tofar
 */
public class GreaterEqual<C extends Comparable> implements Criteria {

    final ValElement<C> element;

    @ConstructorProperties({"value"})
    public GreaterEqual(ValElement<C> element) {
        this.element = element;
    }

    public ValElement<C> getValue() {
        return element;
    }

    @Override
    public boolean match(Element element) {
        if(this.element == null){
            return (element == null); // nulls are last
        }
        if (element instanceof ValElement) {
            ValElement<?> v = (ValElement) element;
            if((v.getType() == ElementType.NUMBER) && (this.element.getType() == ElementType.NUMBER)){
                return v.asNum().getNum() >= this.element.asNum().getNum();
            }else{
                return v.asStr().getStr().compareTo(this.element.asStr().getStr()) >= 0;
            }
        }
        return false;
    }

}
