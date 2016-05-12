package org.om.schema;

import java.util.Locale;
import org.jayson.JaysonInput;
import org.jayson.JaysonType;
import org.om.criteria.Criteria;
import org.om.element.Element;
import org.om.element.ElementType;
import org.om.element.StrElement;

/**
 *
 * @author tofarrell
 */
public final class StrSchema extends Schema {

    public StrSchema(String title, String description, StrElement defaultValue, Criteria criteria) {
        super(title, description, defaultValue, criteria);
    }

    @Override
    public ElementType getType() {
        return ElementType.STRING;
    }

    @Override
    public String getErrMsg(Locale locale, Element element) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Element sanitize(Element element) {
        return (criteria == null) || criteria.match(element)
            
        }
    }



}
