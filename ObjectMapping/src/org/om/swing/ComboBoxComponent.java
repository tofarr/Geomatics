package org.om.swing;

import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import org.om.criteria.Criteria;
import org.om.element.Element;
import org.om.element.StrElement;
import org.om.element.ValElement;
import org.om.schema.ValidationResult;

/**
 *
 * @author tofarrell
 */
public class ComboBoxComponent extends OMComponent {

    private final JComboBox comboBox;
    private final Criteria criteria;

    public ComboBoxComponent(String title, String description, Criteria criteria, String... values) {
        this.comboBox = new JComboBox(values);
        this.criteria = criteria;

        JLabel label = new JLabel(title);
        label.setLabelFor(comboBox);
        label.setToolTipText(description);
        comboBox.setToolTipText(description);
        setLayout(new GridLayout(2, 1));
        add(label);
        add(comboBox);
    }

    @Override
    public void setElement(Element element) {
        ValElement val = (ValElement) element;
        comboBox.getModel().setSelectedItem(val.asStr().getStr());
    }

    @Override
    public Element getElement() {
        Object selected = comboBox.getSelectedItem();
        return (selected == null) ? null : StrElement.valueOf((String) comboBox.getSelectedItem());
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
        comboBox.requestFocus();
    }

    @Override
    public void addActionListener(ActionListener listener) {
        comboBox.addActionListener(listener);
    }

    @Override
    public void removeActionListener(ActionListener listener) {
        comboBox.removeActionListener(listener);
    }

}
