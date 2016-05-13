package org.om.schema.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;
import org.om.element.Element;
import org.om.element.ObjElement;
import org.om.html.HtmlWriter;
import org.om.schema.Path;
import org.om.schema.Schema;
import org.om.schema.ValidationResult;
import org.om.swing.OMComponent;

/**
 *
 * @author tofar
 */
public class ObjSchema extends Schema<ObjElement> {

    private Map<String, Schema> children;
    private ObjElement defaultValue;

    public ObjSchema(String title, String description, Map<String, Schema> children, ObjElement defaultValue) {
        super(title, description);
        this.children = Collections.unmodifiableMap(new LinkedHashMap<>(children));
        this.defaultValue = defaultValue;
    }

    @Override
    public ValidationResult validate(Path path, ObjElement element, ResourceBundle messages) {
        for (Entry<String, Schema> entry : children.entrySet()) {
            Path childPath = path.add(entry.getKey());

        }
        return ValidationResult.SUCCESS;
    }

    @Override
    public ObjElement getDefaultValue() {
        return defaultValue;
    }

    @Override
    public OMComponent toSwingComponent(ObjElement element) {

    }

    @Override
    public void toHtml(Path path, HtmlWriter writer, ResourceBundle resources, ObjElement element) throws IOException {
        zzz
    }

    @Override
    public ObjElement fromFormParams(Path path, HttpServletRequest request) {
        Map<String,Element> elements = new LinkedHashMap<>();
        for (Entry<String, Schema> entry : children.entrySet()) {
            String key = entry.getKey();
            Path childPath = path.add(key);
            Element element = entry.getValue().fromFormParams(childPath, request);
            elements.put(key, element);
        }
        return ObjElement.valueOf(elements);
    }

}
