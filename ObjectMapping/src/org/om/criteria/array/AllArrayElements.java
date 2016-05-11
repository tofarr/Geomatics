package org.om.criteria.array;

import java.beans.ConstructorProperties;
import org.om.criteria.Criteria;
import org.om.element.ArrElement;
import org.om.element.Element;

/**
 *
 * @author tofar
 */
public class AllArrayElements implements Criteria {

    private final Criteria criteria;

    @ConstructorProperties({"criteria"})
    public AllArrayElements(Criteria criteria) throws NullPointerException {
        if(criteria == null){
            throw new NullPointerException("criteria must not be null");
        }
        this.criteria = criteria;
    }

    public Criteria getCriteria() {
        return criteria;
    }

    @Override
    public boolean match(Element element) {
        if(element instanceof ArrElement){
            ArrElement array = (ArrElement)element;
            for(Element e : array){
                if(!criteria.match(e)){
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
