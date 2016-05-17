
package org.om.store;

import org.om.criteria.Criteria;
import org.om.sort.Sorter;

/**
 *
 * @author tofar
 */
public class ReadOnlyElementStore implements ElementStore {
    
    private final WritableElementStore store;

    public ReadOnlyElementStore(WritableElementStore store) throws NullPointerException {
        if(store == null){
            throw new NullPointerException();
        }
        this.store = store;
    }

    @Override
    public boolean load(Criteria criteria, Sorter sorter, ElementProcessor processor) throws StoreException {
        return store.load(criteria, sorter, processor);
    }

    @Override
    public long count(Criteria criteria) throws StoreException {
        return store.count(criteria);
    }
    
    
}
