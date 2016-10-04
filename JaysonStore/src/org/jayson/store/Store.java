
package org.jayson.store;

import org.jayson.element.Element;
import org.jayson.store.listener.EditListener;
import org.jayson.store.listener.EditListenerRule;
import org.jayson.store.security.AuthenticationToken;
import org.jayson.store.security.EntitlementRule;
import org.jayson.store.security.Entitlements;
import org.jayson.store.security.AuthorizationToken;

/**
 *
 * @author tofarr
 */
public interface Store {
    
    //can supply sorting element processor
    boolean query(AuthorizationToken token, Query query, ResultProcessor<Element> processor) throws StoreException;
    
    
    long count(AuthorizationToken token, Query query) throws StoreException;

    
    Edit edit(AuthorizationToken token, Edit edit) throws StoreException;

    //replaces existing locks
    void lock(AuthorizationToken token, Path path, boolean preventRead, long timeout) throws StoreException;
    
    
    void unlock(AuthorizationToken token, Path path) throws StoreException;
    
    
    
    
    Entitlements getEntitlements(AuthorizationToken token, Path path) throws StoreException;
    
    
    boolean queryEntitlementRules(AuthorizationToken token, Path path, ResultProcessor<EntitlementRule> processor) throws StoreException;
    
    
    EntitlementRule addEntitlementRule(AuthorizationToken token, Path path, int priority, Entitlements entitlements, boolean merge) throws StoreException;
    
    
    boolean removeEntitlementRule(AuthorizationToken token, long ruleId) throws StoreException;
    
    
    
    
    boolean queryEditListenerRules(AuthorizationToken token, Path path, ResultProcessor<EditListenerRule> processor) throws StoreException;
    
    
    EditListenerRule addEditListenerRule(AuthorizationToken token, Path path, int priority, EditListener listener) throws StoreException;
    
    
    boolean removeEditListenerRule(AuthorizationToken token, long ruleId) throws StoreException;
    
    
    AuthorizationToken authenticate(AuthenticationToken token);

}
