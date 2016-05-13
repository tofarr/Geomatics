package org.om.schema;

import java.io.IOException;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;
import org.om.element.Element;
import org.om.html.HtmlWriter;
import org.om.swing.OMComponent;

/**
 *
 * @author tofar
 */
public abstract class Schema {

    private final String title;
    private final String description;

    public Schema(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public abstract ValidationResult validate(Path path, Element element, ResourceBundle messages);

    public abstract Element getDefaultValue();

    public abstract OMComponent toSwingComponent(Element element);

    public abstract void toHtml(Path path, HtmlWriter writer, ResourceBundle resources, Element element) throws IOException;

    public abstract Element fromFormParams(Path path, HttpServletRequest request);
}
