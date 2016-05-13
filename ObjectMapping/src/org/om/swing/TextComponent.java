package org.om.swing;

import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.om.criteria.Criteria;
import org.om.element.Element;
import org.om.element.StrElement;
import org.om.element.ValElement;
import org.om.schema.ValidationResult;

/**
 *
 * @author tofarrell
 */
public class TextComponent extends OMComponent {

    private final JTextComponent textComponent;
    private final Criteria criteria;
    private final Border validBorder;
    private final Border invalidBorder;

    public TextComponent(String title, String description, Criteria criteria, boolean useArea) {
        this(title, description, criteria, useArea, INVALID_BORDER);
    }

    public TextComponent(String title, String description, Criteria criteria, boolean useArea, Border invalidBorder) {

        this.textComponent = useArea ? new JTextArea() : new JTextField();
        this.criteria = criteria;
        this.validBorder = textComponent.getBorder();
        this.invalidBorder = invalidBorder;

        JLabel label = new JLabel(title);
        label.setLabelFor(textComponent);
        label.setToolTipText(description);
        textComponent.setToolTipText(description);
        setLayout(new GridLayout(2, 1));
        add(label);
        add(textComponent);
        textComponent.getDocument().addDocumentListener(docListener);
    }

    @Override
    public void setElement(Element element) {
        ValElement val = (ValElement) element;
        StrElement str = val.asStr();
        textComponent.setText(str.getStr());
    }

    @Override
    public Element getElement() {
        return StrElement.valueOf(textComponent.getText());
    }

    @Override
    public void requestFocus() {
        textComponent.requestFocus();
    }

    @Override
    public ValidationResult validateContent(ResourceBundle resources) {
        if ((criteria != null) && (!criteria.match(getElement()))) {
            return new ValidationResult(false, criteria.getDescription(resources));
        }
        return ValidationResult.SUCCESS;
    }

    @Override
    public void addActionListener(ActionListener listener) {
        if (textComponent instanceof JTextField) {
            ((JTextField) textComponent).addActionListener(listener);
        }
    }

    @Override
    public void removeActionListener(ActionListener listener) {
        if (textComponent instanceof JTextField) {
            ((JTextField) textComponent).removeActionListener(listener);
        }
    }

    protected void updateValidation() {
        if ((criteria != null) && (!criteria.match(getElement()))) {
            textComponent.setBorder(invalidBorder);
        } else {
            textComponent.setBorder(validBorder);
        }
    }

    private final DocumentListener docListener = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            updateValidation();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            updateValidation();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            updateValidation();
        }
    };

}
