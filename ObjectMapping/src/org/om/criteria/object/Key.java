package org.om.criteria.object;

import java.beans.ConstructorProperties;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import org.om.criteria.Criteria;
import org.om.element.Element;
import org.om.element.ObjElement;

/**
 *
 * @author tofar
 */
public class Key implements Criteria {

    private String key;
    private Criteria criteria;

    @ConstructorProperties({"key", "criteria"})
    public Key(String key, Criteria criteria) {
        if (key == null) {
            throw new NullPointerException("key must not be null!");
        }
        if (criteria == null) {
            throw new NullPointerException("criteria must not be null!");
        }
        this.key = key;
        this.criteria = criteria;
    }

    public String getKey() {
        return key;
    }

    public Criteria getCriteria() {
        return criteria;
    }

    @Override
    public boolean match(Element element) {
        if (element instanceof ObjElement) {
            ObjElement obj = (ObjElement) element;
            element = obj.getElement(key);
            return criteria.match(element);
        }
        return false;
    }
    
    @Override
    public String getDescription(ResourceBundle resources) {
        String msg = resources.getString("CRITERIA_DESC_KEY");
        msg = MessageFormat.format(msg, key);
        String criteriaDesc = criteria.getDescription(resources);
        return msg+System.lineSeparator()+Criteria.indent(criteriaDesc);
    }
}
