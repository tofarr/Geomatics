package org.jayson.store.listener;

import org.jayson.store.EditContext;
import org.jayson.store.Store;
import org.jayson.store.security.AuthorizationToken;

/**
 *
 * @author tofarr
 */
public class ValidationEditListener implements EditListener{

    
    @Override
    public EditContext execute(Store store, AuthorizationToken token, EditContext edit) throws TriggerException {
        
    }
    
}
