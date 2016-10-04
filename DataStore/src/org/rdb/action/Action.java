package org.rdb.action;

import org.rdb.NodePath;
import org.rdb.Workspace;
import org.rdb.security.SecurityContext;

/**
 *
 * @author tofarr
 * @param <A>
 */
public abstract class Action<A extends ActionResult> {

    public final NodePath path;

    public Action(NodePath path) {
        this.path = path;
    }
    
    public abstract A execute(SecurityContext context, Workspace workspace);
}
