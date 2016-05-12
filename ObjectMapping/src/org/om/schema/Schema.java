package org.om.schema;

import javax.servlet.http.HttpServletRequest;
import org.jayson.JaysonType;
import org.om.element.Element;
import org.om.swing.FormComponent;

/**
 *
 * @author tofar
 */
public interface Schema {
    
    JaysonType getElementType();
    
    ValidationResult validate(Path path, Element e);
    
    Element getDefaultValue();
    
    FormComponent toFormComponent();
    
    void toFormHtml(Path path, Appendable appendable);
    
    Element fromForm(Path path, HttpServletRequest request);
}
