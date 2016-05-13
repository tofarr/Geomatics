package org.om.swing;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;
import org.om.element.Element;
import org.om.schema.ValidationResult;

/**
 *
 * @author tofarrell
 */
public abstract class OMComponent extends JComponent {

    public static final String ELEMENT = "element";
    public static final Border INVALID_BORDER = BorderFactory.createLineBorder(Color.RED, 2);

    public abstract void setElement(Element element);

    public abstract Element getElement();
    
    public abstract ValidationResult validateContent(ResourceBundle resources);
    
    public abstract void requestFocus();
    
    public abstract void addActionListener(ActionListener listener);

    public abstract void removeActionListener(ActionListener listener);
}
