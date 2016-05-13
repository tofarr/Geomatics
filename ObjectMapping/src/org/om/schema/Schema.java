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
public abstract class Schema<E extends Element> {

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

    public abstract ValidationResult validate(Path path, E element, ResourceBundle messages);

    public abstract E getDefaultValue();

    public abstract OMComponent toSwingComponent(E element);

    public abstract void toHtml(Path path, HtmlWriter writer, ResourceBundle resources, E element) throws IOException;

    public abstract E fromFormParams(Path path, HttpServletRequest request);
}
