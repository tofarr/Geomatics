
package org.rdb.action;

import java.beans.ConstructorProperties;

/**
 *
 * @author tofarr
 */
public class RemoveActionResult  extends ActionResult<RemoveAction> {

    public final boolean removed;

    @ConstructorProperties({"action", "removed"})
    public RemoveActionResult(GetAction action, boolean removed) {
        super(action);
        this.removed = removed;
    }
}
