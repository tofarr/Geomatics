package org.om.criteria;

import java.beans.ConstructorProperties;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.ResourceBundle;
import org.om.element.Element;
import org.om.element.ValElement;

/**
 *
 * @author tofar
 */
public class Equal implements Criteria {

    private final Element element;

    @ConstructorProperties({"element"})
    public Equal(Element element) {
        this.element = element;
    }

    public Element getElement() {
        return element;
    }

    @Override
    public boolean match(Element element) {
        return Objects.equals(this.element, element);
    }

    @Override
    public String getDescription(ResourceBundle resources) {
        String value = resources.getString("CRITERIA_DESC_EQUAL");
        String param;
        if(element instanceof ValElement){
            param = element.toString();
        }else if(element == null){
            param = "null";
        }else{
            param = resources.getString("PRESET_VALUE");
        }
        return MessageFormat.format(value, param);
    }
}
