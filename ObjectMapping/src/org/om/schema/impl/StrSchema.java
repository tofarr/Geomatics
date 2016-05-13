package org.om.schema.impl;

import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.om.criteria.And;
import org.om.criteria.Criteria;
import org.om.criteria.Length;
import org.om.criteria.value.Less;
import org.om.criteria.value.LessEqual;
import org.om.element.Element;
import org.om.element.ElementType;
import org.om.element.StrElement;
import org.om.element.ValElement;
import org.om.schema.Path;
import org.om.schema.Schema;
import org.om.schema.ValidationResult;
import org.om.swing.OMComponent;

/**
 *
 * @author tofarrell
 */
public class StrSchema extends Schema {

    private final Criteria criteria;
    private final StrElement defaultValue;

    public StrSchema(String title, String description, Criteria criteria, StrElement defaultValue) {
        super(title, description);
        this.criteria = criteria;
        this.defaultValue = defaultValue;
    }

    public Criteria getCriteria() {
        return criteria;
    }

    @Override
    public ElementType getElementType() {
        return ElementType.STRING;
    }

    @Override
    public ValidationResult validate(Path path, Element element, ResourceBundle messages) {
        if (criteria.match(element)) {
            return ValidationResult.SUCCESS;
        }
        return new ValidationResult(false, criteria.getDescription(messages));
    }

    @Override
    public Element getDefaultValue() {
        return defaultValue;
    }

    @Override
    public OMComponent toSwingComponent(Element element) {
        return new StrComponent(getTitle(), getDescription(), criteria);
    }

    NEED SPECIAL HTML FRAMEWORK - JS WRITER, JS CONTEXT AND HTMLWRITER
    
    @Override
    public void toHtml(Path path, Appendable appendable, Element element) throws IOException {
        appendable.append("<div id=\"").append(path.toString()).append("_div\" class=\"om_")
                .append(getClass().getSimpleName()).append('"');
        if(getDescription() != null){
            appendable.append(" title=\"").append(escapeAttr(getDescription())).append('"');
        }
        appendable.append(">");
        if(getTitle() != null){
            appendable.append("<label for=\"").append(path.toString()).append("\">").append(escapeText(title)).append("</label>");
        }
        int maxLength = getMaxLength(criteria); // detect max length based on criteria, and use either a text field or a text area accordingly
        if ((maxLength <= 200) || (maxLength == Integer.MAX_VALUE)) {
            appendable.append("<input type=\"text\" id=\"").append(path.toString())
                    .append("\" name=\"").append(path.toString())
                    .append("\" value=\"").append(escapeAttr(((ValElement)element).asStr().getStr()))
                    if(criteria != null){
                        
                    }
                    .append("\" />");
        } else {
            textComponent = new JTextArea();
        }
        appendable.append("</div>");
    }

    @Override
    public Element fromFormParams(Path path, HttpServletRequest request) {
        String ret = request.getParameter(path.toString());
        return (ret == null) ? null : StrElement.valueOf(ret);
    }

    public static int getMaxLength(Criteria criteria) {
        if (criteria instanceof Length) {
            Length len = (Length) criteria;
            return getMaxValue(len.getCriteria());
        } else if (criteria instanceof And) {
            int ret = Integer.MAX_VALUE;
            And and = (And) criteria;
            for (int i = and.numCriteria(); i-- > 0;) {
                ret = Math.min(ret, getMaxLength(and.getCriteria(i)));
            }
            return ret;
        } else {
            return Integer.MAX_VALUE;
        }
    }

    public static int getMaxValue(Criteria criteria) {
        if (criteria instanceof Less) {
            ValElement val = ((LessEqual) criteria).getValue();
            return getValue(val);
        } else if (criteria instanceof LessEqual) {
            int ret = getValue(((Less) criteria).getValue());
            return (ret == Integer.MAX_VALUE) ? ret : (ret + 1);
        } else {
            return Integer.MAX_VALUE;
        }
    }

    public static int getValue(ValElement element) {
        try {
            double num = element.asNum().getNum();
            int ret = (int) num;
            return (ret == num) ? ret : Integer.MAX_VALUE;
        } catch (Exception ex) {
            return Integer.MAX_VALUE;
        }
    }

    public static class StrComponent extends OMComponent {

        private final JTextComponent textComponent;
        private final Criteria criteria;
        private final Border validBorder;
        private final Border invalidBorder;
        private StrElement element;

        public StrComponent(String title, String description, Criteria criteria) {
            this(title, description, criteria, INVALID_BORDER);
        }

        public StrComponent(String title, String description, Criteria criteria, Border invalidBorder) {

            int maxLength = getMaxLength(criteria); // detect max length based on criteria, and use either a text field or a text area accordingly
            if ((maxLength <= 200) || (maxLength == Integer.MAX_VALUE)) {
                textComponent = new JTextField();
            } else {
                textComponent = new JTextArea();
            }
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
            if (element instanceof ValElement) {
                ValElement val = (ValElement) element;
                StrElement str = val.asStr();
                if (criteria != null) {
                    textComponent.setBorder(criteria.match(element) ? validBorder : invalidBorder);
                }
                firePropertyChange(ELEMENT, this.element, this.element = str);
            }
        }

        @Override
        public Element getElement() {
            return element;
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

        private final DocumentListener docListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setElement(StrElement.valueOf(textComponent.getText()));
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setElement(StrElement.valueOf(textComponent.getText()));
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setElement(StrElement.valueOf(textComponent.getText()));
            }
        };
    }
}
