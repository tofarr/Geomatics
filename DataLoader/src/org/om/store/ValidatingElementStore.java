package org.om.store;

import java.util.List;
import org.om.attr.AttrSet;
import org.om.criteria.All;
import org.om.criteria.And;
import org.om.criteria.Criteria;
import org.om.element.Element;
import org.om.element.ObjElement;
import org.om.sort.Sorter;

/**
 *
 * @author tofarrell
 */
public class ValidatingElementStore implements ElementStore {

    private final Criteria validator;
    private final ElementStore store;

    public ValidatingElementStore(Criteria validator, ElementStore store) {
        if(validator == null){
            throw new NullPointerException("validator must not be null!");
        }
        if(store == null){
            throw new NullPointerException("Store must not be null!");
        }
        this.validator = validator;
        this.store = store;
    }
    
    public static ElementStore valueOf(Criteria criteria, ElementStore store){
        if(criteria == All.INSTANCE){
            return store;
        }else{
            return new ValidatingElementStore(criteria, store);
        }
    }

    @Override
    public Capabilities getCapabilities() {
        return store.getCapabilities();
    }

    @Override
    public AttrSet getAttrs() {
        return store.getAttrs();
    }
    
    @Override
    public boolean load(List<String> attrs, Criteria criteria, Sorter sorter, ElementProcessor processor) throws StoreException {
        criteria = And.valueOf(this.validator, criteria);
        return store.load(attrs, criteria, sorter, processor);
    }

    @Override
    public long count(Criteria criteria) throws StoreException {
        criteria = And.valueOf(this.validator, criteria);
        return store.count(criteria);
    }

    @Override
    public ObjElement create(ObjElement element) throws StoreException {
        if(!validator.match(element)){
            throw new StoreException("Could not create item in store!");
        }
        return store.create(element);
    }

    @Override
    public long update(Criteria criteria, ObjElement element) throws StoreException {
        criteria = And.valueOf(this.validator, criteria);
        return store.update(criteria, element);
    }

    @Override
    public long remove(Criteria criteria) throws StoreException {
        criteria = And.valueOf(this.validator, criteria);
        return store.remove(criteria);
    }

    @Override
    public void createAll(List<ObjElement> elements) throws StoreException {
        for(ObjElement element : elements){
            if(!validator.match(element)){
                throw new StoreException("Could not create item in store!");
            }
        }
        store.createAll(elements);
    }
}
