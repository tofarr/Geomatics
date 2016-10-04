
package org.rdb.trigger;

import org.rdb.NodeException;
import org.rdb.NodePath;
import org.rdb.Workspace;
import org.rdb.security.SecurityContext;

/**
 *
 * @author tofarr
 */
public interface Trigger<E> {
    
    void exec(SecurityContext context, TriggerEvent event, Workspace workspace, NodePath path) throws NodeException;
    
    void exec(SecurityContext context, TriggerEvent event, Workspace workspace, NodePath path) throws NodeException;
}
