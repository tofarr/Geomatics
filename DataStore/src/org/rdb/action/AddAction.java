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
public class AddAction extends Action<AddActionResult> {

    public final String id;
    public final Element element;

    @ConstructorProperties({"path","id","element"})
    public AddAction(NodePath path, String id, Element element) {
        super(path);
        this.id = id;
        this.element = element;
    }

    @Override
    public AddActionResult execute(SecurityContext context, Workspace workspace) {
        Node node = workspace.findNode(context, path);
        String newId;
        if(id == null){
            newId = node.addChild(context, element);
        }else{
            node.addChild(context, id, element);
            newId = id;
        }
        return new AddActionResult(this, path.add(newId));
    }

}
