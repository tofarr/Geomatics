package org.om.criteria.array;

import java.beans.ConstructorProperties;
import java.util.ResourceBundle;
import org.om.criteria.Criteria;
import org.om.element.ArrElement;
import org.om.element.Element;

/**
 *
 * @author tofar
 */
public class AllArrayItems implements Criteria {

    private final Criteria criteria;

    @ConstructorProperties({"criteria"})
    public AllArrayItems(Criteria criteria) throws NullPointerException {
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
    
    @Override
    public String getDescription(ResourceBundle resources) {
        String msg = resources.getString("CRITERIA_DESC_ALL_ARRAY_ITEMS");
        String criteriaDesc = criteria.getDescription(resources);
        return msg+System.lineSeparator()+Criteria.indent(criteriaDesc);
    }
}