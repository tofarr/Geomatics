package org.rdb.action;

import java.beans.ConstructorProperties;
import java.util.List;
import org.rdb.Node;
import org.rdb.NodePath;
import org.rdb.Workspace;
import org.rdb.criteria.Criteria;
import org.rdb.security.SecurityContext;
import org.rdb.sorter.Sorter;

/**
 *
 * @author tofarr
 */
public class QueryAction extends Action<QueryActionResult> {

    public final Criteria criteria;
    public final Sorter sorter;
    public final long offset;
    public final int length;
    
    @ConstructorProperties({"path", "criteria", "sorter", "offset", "length"})
    public QueryAction(NodePath path, Criteria criteria, Sorter sorter, long offset, int length) {
        super(path);
        this.criteria = criteria;
        this.sorter = sorter;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public QueryActionResult execute(SecurityContext context, Workspace workspace) {
        Node node = workspace.findNode(context, path);
        List<Node> results = node.query(context, criteria, sorter, offset, length);
        return new QueryActionResult(this, results);
    }

}
