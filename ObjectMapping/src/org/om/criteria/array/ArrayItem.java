package org.om.criteria.array;

import java.beans.ConstructorProperties;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import org.om.criteria.Criteria;
import org.om.element.ArrElement;
import org.om.element.Element;
import org.om.element.ValElement;

/**
 *
 * @author tofar
 */
public class ArrayItem implements Criteria {

    private final int index;
    private final Criteria criteria;

    @ConstructorProperties({"index", "criteria"})
    public ArrayItem(int index, Criteria criteria) throws NullPointerException, IllegalArgumentException {
        if (index < 0) {
            throw new IllegalArgumentException("Invalid index : " + index);
        }
        if (criteria == null) {
            throw new NullPointerException("criteria must not be null");
        }
        this.index = index;
        this.criteria = criteria;
    }

    public int getIndex() {
        return index;
    }

    public Criteria getCriteria() {
        return criteria;
    }

    @Override
    public boolean match(Element element) {
        if (element instanceof ArrElement) {
            ArrElement array = (ArrElement) element;
            if(array.size() > index){
                element = array.valueAt(index);
                return criteria.match(element);
            }
        }
        return false;
    }
    
    @Override
    public String getDescription(ResourceBundle resources) {
        String msg = resources.getString("CRITERIA_DESC_ARRAY_ITEM");
        msg = MessageFormat.format(msg, Integer.toString(index));
        String criteriaDesc = criteria.getDescription(resources);
        return msg+System.lineSeparator()+Criteria.indent(criteriaDesc);
    }
}