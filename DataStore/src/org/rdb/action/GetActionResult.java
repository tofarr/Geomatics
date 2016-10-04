package org.rdb.action;

import java.beans.ConstructorProperties;
import org.jayson.element.Element;

/**
 *
 * @author tofarr
 */
public class GetActionResult extends ActionResult<GetAction> {

    public final Element element;

    @ConstructorProperties({"action", "element"})
    public GetActionResult(GetAction action, Element element) {
        super(action);
        this.element = element;
    }
}
