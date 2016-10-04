package org.rdb.action;

import java.beans.ConstructorProperties;
import org.rdb.NodePath;
import org.rdb.Workspace;
import org.rdb.security.SecurityContext;

/**
 *
 * @author tofarr
 */
public class RemoveAction extends Action<RemoveActionResult> {

    @ConstructorProperties({"path"})
    public RemoveAction(NodePath path) {
        super(path);
    }

    @Override
    public GetActionResult execute(SecurityContext context, Workspace workspace) {
        Node node = workspace.findNode(context, path);
        workspace.findNode
        Element element = node.get(context);
        return new GetActionResult(this, element);
    }

}
