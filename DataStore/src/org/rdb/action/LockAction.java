package org.rdb.action;

import java.beans.ConstructorProperties;
import org.rdb.Node;
import org.rdb.NodePath;
import org.rdb.Workspace;
import org.rdb.security.SecurityContext;

/**
 *
 * @author tofarr
 */
public class LockAction extends Action<LockActionResult> {

    private final boolean preventRead;
    private final long timeout;

    @ConstructorProperties({"path", "preventRead", "timeout"})
    public LockAction(NodePath path, boolean preventRead, long timeout) {
        super(path);
        this.preventRead = preventRead;
        this.timeout = timeout;
    }

    @Override
    public LockActionResult execute(SecurityContext context, Workspace workspace) {
        Node node = workspace.findNode(context, path);
        node.lock(context, preventRead, 0);
        return new LockActionResult(this);
    }

}
