package org.rdb;

import org.rdb.security.AuthenticationToken;
import org.rdb.security.SecurityContext;

/**
 *
 * @author tofarr
 */
public interface Workspace {

    NodeTemplate findTemplate(SecurityContext context, NodePath path);

    void updateTemplate(SecurityContext context, NodePath path);

    Node getRoot();
    
    Node findNode(SecurityContext context, NodePath path);
   
    /**
     * Obtain a security context.
     * @param token the token (may be password based, or key pair based
     * @return
     * 
     */
    SecurityContext authenticate(AuthenticationToken token);
    
    void addTrigger();
}
