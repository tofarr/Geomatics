package org.rdb.action;

import java.beans.ConstructorProperties;
import org.jayson.element.Element;
import org.rdb.Node;
import org.rdb.NodePath;
import org.rdb.Workspace;
import org.rdb.security.SecurityContext;

/**
 *
 * @author tofarr
 */
public class GetAction extends Action<GetActionResult> {

    @ConstructorProperties({"path"})
    public GetAction(NodePath path) {
        super(path);
    }

    @Override
    public GetActionResult execute(SecurityContext context, Workspace workspace) {
        Node node = workspace.findNode(context, path);
        Element element = node.get(context);
        return new GetActionResult(this, element);
    }

}
