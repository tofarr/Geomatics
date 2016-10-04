package org.rdb.action;

import java.beans.ConstructorProperties;
import org.rdb.NodePath;

/**
 *
 * @author tofarr
 */
public class AddActionResult extends ActionResult<AddAction> {

    public final NodePath path;

    @ConstructorProperties({"action", "path"})
    public AddActionResult(AddAction action, NodePath path) {
        super(action);
        this.path = path;
    }
}
