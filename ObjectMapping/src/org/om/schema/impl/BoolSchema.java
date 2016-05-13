package org.om.schema.impl;

import java.io.IOException;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;
import org.om.criteria.Criteria;
import org.om.element.BoolElement;
import org.om.element.Element;
import org.om.element.ValElement;
import org.om.html.HtmlWriter;
import org.om.schema.Path;
import org.om.schema.Schema;
import org.om.schema.ValidationResult;
import org.om.swing.CheckBoxComponent;
import org.om.swing.OMComponent;

/**
 *
 * @author tofar
 */
public class BoolSchema extends Schema<BoolElement> {

    private final Criteria criteria;
    private final BoolElement defaultValue;

    public BoolSchema(Criteria criteria, BoolElement defaultValue, String title, String description) {
        super(title, description);
        this.criteria = criteria;
        this.defaultValue = defaultValue;
    }

    @Override
    public ValidationResult validate(Path path, BoolElement element, ResourceBundle messages) {
        if (criteria.match(element)) {
            return ValidationResult.SUCCESS;
        }
        return new ValidationResult(false, criteria.getDescription(messages));
    }

    @Override
    public BoolElement getDefaultValue() {
        return defaultValue;
    }

    @Override
    public OMComponent toSwingComponent(BoolElement element) {
        return new CheckBoxComponent(getTitle(), getDescription(), criteria);
    }

    @Override
    public void toHtml(Path path, HtmlWriter writer, ResourceBundle resources, BoolElement element) throws IOException {
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
                .attr("type", "checkbox")
                .attr("id", id)
                .attr("name", pathStr);
        if (element != null) {
            writer.attr("checked", Boolean.toString(element.isBool()));
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
    public BoolElement fromFormParams(Path path, HttpServletRequest request) {
        String ret = request.getParameter(path.toString());
        return (ret == null) ? null : BoolElement.valueOf(Boolean.parseBoolean(ret));
    }
}
