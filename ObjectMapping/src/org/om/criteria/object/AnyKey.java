package org.om.criteria.object;

import java.beans.ConstructorProperties;
import java.util.ResourceBundle;
import org.om.criteria.Criteria;
import org.om.element.Element;
import org.om.element.ObjElement;

/**
 *
 * @author tofar
 */
public class AnyKey  implements Criteria {

    private final Criteria criteria;

    @ConstructorProperties({"criteria"})
    public AnyKey(Criteria criteria) throws NullPointerException {
        if (criteria == null) {
            throw new NullPointerException("criteria must not be null");
        }
        this.criteria = criteria;
    }

    public Criteria getCriteria() {
        return criteria;
    }

    @Override
    public boolean match(Element element) {
        if (element instanceof ObjElement) {
            ObjElement obj = (ObjElement) element;
            for (String key : obj) {
                Element e = obj.getElement(key);
                if (criteria.match(e)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public String getDescription(ResourceBundle resources) {
        String msg = resources.getString("CRITERIA_DESC_ANY_KEY");
        String criteriaDesc = criteria.getDescription(resources);
        return msg+System.lineSeparator()+Criteria.indent(criteriaDesc);
    }
}
