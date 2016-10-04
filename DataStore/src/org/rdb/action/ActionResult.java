package org.rdb.action;

/**
 *
 * @author tofarr
 * @param <A>
 */
public abstract class ActionResult<A extends Action> {

    public final A action;

    public ActionResult(A action) {
        this.action = action;
    }
}
