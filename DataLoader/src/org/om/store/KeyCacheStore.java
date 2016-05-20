package org.om.store;

import org.om.criteria.Criteria;
import org.om.criteria.Equal;
import org.om.criteria.object.Key;

/**
 *
 * @author tofarrell
 */
public class KeyCacheStore extends AbstractCacheStore {

    private final String key;
    
    public KeyCacheStore(ElementStore store, String key) {
        super(store);
        if(key == null){
            throw new NullPointerException("key must not be null");
        }
        this.key = key;
    }

    @Override
    public boolean isCachable(Criteria criteria) {
        if(criteria instanceof Key){
            Key key = (Key)criteria;
            if(key.equals(key.getKey())){
                return (key.getCriteria() instanceof Equal);
            }
        }
        return false;
    }
}
