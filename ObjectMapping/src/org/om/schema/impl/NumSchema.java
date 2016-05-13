package org.om.schema.impl;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;
import org.om.criteria.Criteria;
import org.om.element.NumElement;
import org.om.element.ValElement;
import org.om.html.HtmlWriter;
import org.om.schema.Path;
import org.om.schema.Schema;
import org.om.schema.ValidationResult;
import static org.om.schema.impl.StrSchema.getComboBoxValues;
import static org.om.schema.impl.StrSchema.getMaxLength;
import org.om.swing.ComboBoxComponent;
import org.om.swing.OMComponent;
import org.om.swing.TextComponent;

/**
 *
 * @author tofar
 */
public class NumSchema extends Schema<NumElement> {
    
    private final Criteria criteria;
    private final NumElement defaultValue;
    

    @ConstructorProperties({"title", "description", "criteria", "defaultValue"})
    public NumSchema(String title, String description, Criteria criteria, NumElement defaultValue) {
        super(title, description);
        this.criteria = criteria;
        this.defaultValue = defaultValue;
    }

    @Override
    public ValidationResult validate(Path path, NumElement element, ResourceBundle messages) {
        if (criteria.match(element)) {
            return ValidationResult.SUCCESS;
        }
        return new ValidationResult(false, criteria.getDescription(messages));
    }

    @Override
    public NumElement getDefaultValue() {
        return defaultValue;
    }

    @Override
    public OMComponent toSwingComponent(NumElement element) {
        List<String> values = getComboBoxValues(criteria);
        if (values != null) {
            return new ComboBoxComponent(getTitle(), getDescription(), criteria, values.toArray(new String[values.size()]));
        }
        int maxLength = getMaxLength(criteria);
        TextComponent ret = new TextComponent(getTitle(), getDescription(), criteria,
                ((maxLength > 200) && (maxLength != Integer.MAX_VALUE)));
        ret.setElement(element);
        return ret;
    }

    @Override
    public void toHtml(Path path, HtmlWriter writer, ResourceBundle resources, NumElement element) throws IOException {
        String pathStr = path.toString();
        writer.begin("div")
                .attr("id", writer.createId())
                .attr("class", writer.getNamespace() + "_" + getClass().getSimpleName());
        if (getDescription() != null) {
            writer.attr("title", getDescription());
        }
        String id = writer.createId();
        if (getTitle() != null) {
            writer.begin("label").attr("for", id).text(getTitle()).end();
        }
        writer.tag("input")
                .attr("type", "text")
                .attr("id", id)
                .attr("name", pathStr);
        if (element != null) {
            writer.attr("value", Double.toString(((ValElement) element).asNum().getNum()));
        }
        if (criteria != null) {
            writer.js(writer.getNamespace()).js(".validators.").js(id).js("=function(){")
                    .js("var e = $(\"#" + id + "\");")
                    .js("var v = e.val();")
                    .js("var r = ");
            criteria.toJavascript("v", writer.getJavascript());
            writer.js(";")
                    .js("e[r?\"removeClass\":\"addClass\"](\"").js(writer.getNamespace()).js("_invalid\");")
                    .js("return r;")
                    .js("}");
            writer.js(writer.getNamespace()).js(".descriptions.").js(id).js("=\"").js(criteria.getDescription(resources).replace("\"\"", "\\\"")).js("\";");
        }
    }

    @Override
    public NumElement fromFormParams(Path path, HttpServletRequest request) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
