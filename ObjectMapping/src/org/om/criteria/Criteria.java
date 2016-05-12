package org.om.criteria;

import java.util.ResourceBundle;
import org.om.element.Element;

/**
 *
 * @author tofar
 */
public interface Criteria {

    boolean match(Element element);

    String getDescription(ResourceBundle resources);

    public static String indent(String str) {
        return "\t" + str.replace("\n", "\n\t");
    }
}
