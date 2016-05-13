package org.om.swing;

import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.border.Border;
import org.om.element.Element;
import org.om.element.ObjElement;
import org.om.schema.ValidationResult;

/**
 *
 * @author tofar
 */
public class FieldSetComponent extends OMComponent {
    
    private final Map<String,OMComponent> components;

    public FieldSetComponent(String title, Map<String, OMComponent> components) {
        this.components = components;
        if(title != null){
            Border border = BorderFactory.createTitledBorder(title);
            setBorder(border);
        }
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        for(OMComponent component : components.values()){
            add(component);
        }
    }

    @Override
    public void setElement(Element element) {
        ObjElement objElement = (ObjElement)element;
        for(Entry<String,OMComponent> entry : components.entrySet()){
            Element e = objElement.getElement(entry.getKey());
            entry.getValue().setElement(e);
        }
    }

    @Override
    public ObjElement getElement() {
        Map<String,Element> ret = new LinkedHashMap<>();
        for(Entry<String,OMComponent> entry : components.entrySet()){
            Element e = entry.getValue().getElement();
            ret.put(entry.getKey(), e);
        }
        return ObjElement.valueOf(ret);
    }

    @Override
    public ValidationResult validateContent(ResourceBundle resources) {
        for(OMComponent component : components.values()){
            ValidationResult result = component.validateContent(resources);
            if(!result.isSuccess()){
                return result;
            }
        }
        return ValidationResult.SUCCESS;
    }

    @Override
    public void requestFocus() {
        if(!components.isEmpty()){
            components.values().iterator().next().requestFocus();
        }
    }

    @Override
    public void addActionListener(ActionListener listener) {
        for(OMComponent component : components.values()){
            component.addActionListener(listener);
        }
    }

    @Override
    public void removeActionListener(ActionListener listener) {
        for(OMComponent component : components.values()){
            component.removeActionListener(listener);
        }
    }

}
