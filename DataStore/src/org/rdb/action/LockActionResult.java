
package org.rdb.action;

import java.beans.ConstructorProperties;

/**
 *
 * @author tofarr
 */
public class LockActionResult extends ActionResult<LockAction> {

    @ConstructorProperties({"action"})
    public LockActionResult(LockAction action) {
        super(action);
    }
}
