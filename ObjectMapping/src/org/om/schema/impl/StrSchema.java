package org.om.schema.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;
import org.om.criteria.And;
import org.om.criteria.Criteria;
import org.om.criteria.Equal;
import org.om.criteria.Length;
import org.om.criteria.Or;
import org.om.criteria.value.Less;
import org.om.criteria.value.LessEqual;
import org.om.element.Element;
import org.om.element.StrElement;
import org.om.element.ValElement;
import org.om.html.HtmlWriter;
import org.om.schema.Path;
import org.om.schema.Schema;
import org.om.schema.ValidationResult;
import org.om.swing.ComboBoxComponent;
import org.om.swing.OMComponent;
import org.om.swing.TextComponent;

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
        List<String> values = getComboBoxValues(criteria);
        if(values != null){
            return new ComboBoxComponent(getTitle(), getDescription(), criteria, values.toArray(new String[values.size()]));
        }
        int maxLength = getMaxLength(criteria);
        return new TextComponent(getTitle(), getDescription(), criteria, 
                ((maxLength > 200) && (maxLength != Integer.MAX_VALUE)));
    }

    @Override
    public void toHtml(Path path, HtmlWriter writer, ResourceBundle resources, Element element) throws IOException {
        String pathStr = path.toString();
        writer.begin("div")
                .attr("id", writer.createId())
                .attr("class", writer.getNamespace()+"_"+getClass().getSimpleName());
        if(getDescription() != null){
            writer.attr("title", getDescription());
        }
        String id = writer.createId();
        if(getTitle() != null){
            writer.begin("label").attr("for", id).text(getTitle()).end();
        }
        int maxLength = getMaxLength(criteria); // detect max length based on criteria, and use either a text field or a text area accordingly
        if ((maxLength <= 200) || (maxLength == Integer.MAX_VALUE)) {
            writer.tag("input")
                    .attr("type", "text")
                    .attr("id", id)
                    .attr("name", pathStr);
            if(element != null){
                writer.attr("value", ((ValElement)element).asStr().getStr());
            }
        } else {
            writer.begin("textarea")
                    .attr("id", id)
                    .attr("name", pathStr);
            if(element != null){
                writer.text(((ValElement)element).asStr().getStr());
            }
            writer.close();
        }
        if(criteria != null){
            writer.js(writer.getNamespace()).js(".validators.").js(id).js("=function(){")
                .js("var e = $(\"#"+id+"\");")
                .js("var v = e.val();")
                .js("var r = ");
            criteria.toJavascript("v", writer.getJavascript());
            writer.js(";")
                .js("e[r?\"removeClass\":\"addClass\"](\"").js(writer.getNamespace()).js("\");")
                .js("return r;")
                .js("}");
            writer.js(writer.getNamespace()).js(".descriptions.").js(id).js("=\"").js(criteria.getDescription(resources).replace("\"\"", "\\\"")).js("\";");
        }
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
    
    public static List<String> getComboBoxValues(Criteria criteria){
        if(criteria instanceof Or){
            Or or = (Or)criteria;
            List<String> ret = new ArrayList<>();
            for(int i = 0; i < or.numCriteria(); i++){
                Criteria c = or.getCriteria(i);
                if(!(c instanceof Equal)){
                    return null;
                }
                Equal equal = (Equal)c;
                Element element = equal.getElement();
                if(!(element instanceof ValElement)){
                    return null;
                }
                ret.add(((ValElement)element).asStr().getStr());
            }
            return ret;
        }
        return null;
    }
}
