package org.om.swing;

import java.awt.Color;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;
import org.om.element.Element;

/**
 *
 * @author tofarrell
 */
public abstract class OMComponent extends JComponent {

    public static final String ELEMENT = "element";
    public static final Border INVALID_BORDER = BorderFactory.createLineBorder(Color.RED, 2);

    public abstract void setElement(Element element);

    public abstract Element getElement();
    
    public abstract void addActionListener(ActionListener listener);

    public abstract void removeActionListener(ActionListener listener);
}
