package org.om.swing;

import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeListener;
import org.om.criteria.Criteria;
import org.om.element.BoolElement;
import org.om.element.Element;
import org.om.element.ValElement;
import org.om.schema.ValidationResult;

/**
 *
 * @author tofarrell
 */
public class CheckBoxComponent extends OMComponent {

    private final JCheckBox checkbox;
    private final Criteria criteria;

    public CheckBoxComponent(String title, String description, Criteria criteria) {
        this.checkbox = new JCheckBox(title);
        this.criteria = criteria;
        setToolTipText(description);
        setLayout(new FlowLayout());
        add(checkbox);        
    }

    @Override
    public void setElement(Element element) {
        ValElement val = (ValElement) element;
        checkbox.setSelected(val.asBool().isBool());
    }

    @Override
    public Element getElement() {
        return BoolElement.valueOf(checkbox.isSelected());
    }

    @Override
    public ValidationResult validateContent(ResourceBundle resources) {
        if ((criteria != null) && (!criteria.match(getElement()))) {
            return new ValidationResult(false, criteria.getDescription(resources));
        }
        return ValidationResult.SUCCESS;
    }

    @Override
    public void requestFocus() {
        checkbox.requestFocus();
    }

    @Override
    public void addActionListener(ActionListener listener) {
        checkbox.addActionListener(listener);
    }

    @Override
    public void removeActionListener(ActionListener listener) {
        checkbox.removeActionListener(listener);
    }
}
