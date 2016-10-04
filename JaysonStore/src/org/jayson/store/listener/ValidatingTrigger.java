
package org.jayson.store.listener;

import org.jayson.element.Element;
import org.jayson.store.Path;
import org.jayson.store.Store;
import org.jayson.store.criteria.Criteria;
import org.jayson.store.criteria.CriteriaException;
import org.jayson.store.security.AuthorizationToken;

/**
 *
 * @author tofarr
 */
public class ValidatingTrigger implements Trigger {
    
    public final Criteria criteria;

    public ValidatingTrigger(Criteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Element execute(AuthorizationToken context, Store store, Path path, Element oldValue, Element newValue) throws CriteriaException {
        try{
            criteria.check(newValue);
        }catch(CriteriaException ex){
            throw new TriggerException("Invalid Update", ex);
        }
        return newValue;
    }
}
