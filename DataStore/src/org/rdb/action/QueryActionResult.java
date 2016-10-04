
package org.rdb.action;

import java.util.List;
import org.rdb.Node;

/**
 *
 * @author tofarr
 */
public class QueryActionResult extends ActionResult<QueryAction> {
    
    public final List<Node> results;
    
    public QueryActionResult(QueryAction action, List<Node> results) {
        super(action);
        this.results = results;
    }
    
}
