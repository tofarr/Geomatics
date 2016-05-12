package org.om.criteria;

import java.beans.ConstructorProperties;
import java.util.Objects;
import org.om.element.Element;
import org.om.schema.Path;
import org.om.schema.Result;

/**
 *
 * @author tofar
 */
public class Equal implements Criteria {

    private final Element element;

    @ConstructorProperties({"element"})
    public Equal(Element element) {
        this.element = element;
    }

    public Element getElement() {
        return element;
    }

    @Override
    public boolean match(Element element) {
        return Objects.equals(this.element, element);
    }
    
    as it stands, this is too complex. We need to get a simple description of what this does instead
    -we can do this using a resource bundle.
    better to use this than a crazy validation framework

    @Override
    public Result validate(Path path, Element element) {
        if(!match(element)){
            return new Result(path, false, "FAIL_"+getClass().getSimpleName(), new String[]{this.element.toString(), element.toString())
        }
    }
    
    
}
