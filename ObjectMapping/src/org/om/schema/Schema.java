package org.om.schema;

import java.util.Locale;
import org.om.criteria.Criteria;
import org.om.element.Element;
import org.om.element.ElementType;

/**
 *
 * @author tofarrell
 * @param <E>
 */
public abstract class Schema<E extends Element> {

    final String title;
    final String description;

    Schema(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public abstract String vali
    
    public boolean isValid(E element) {
        if (criteria == null) {
            return true; // no criteria - no validation!
        }
        return criteria.match(element);
    }
    
    public abstract String getErrMsg(Locale locale, E element); //what about languages other than english?

    public abstract ElementType getType();

    public abstract E sanitize(E element);
}
