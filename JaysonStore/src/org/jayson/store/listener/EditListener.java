package org.jayson.store.listener;

import org.jayson.store.EditContext;
import org.jayson.store.Store;
import org.jayson.store.security.AuthorizationToken;

/**
 * Triggers are attached to a part of the tree, and called when just before any
 * change occurs to that tree.
 * 
 * Triggers have a priority, where lower numbers are run before higher numbers.
 * 
 * @author tofarr
 */
public interface EditListener {
    
    EditContext execute(Store store, AuthorizationToken token, EditContext edit) throws TriggerException;
    
}
