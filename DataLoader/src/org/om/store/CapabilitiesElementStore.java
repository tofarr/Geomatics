package org.om.store;

import java.util.List;
import org.om.criteria.Criteria;
import org.om.element.Element;
import org.om.sort.Sorter;

/**
 * Element store which wraps another to reduce capabilities
 * @author tofarrell
 */
public class CapabilitiesElementStore implements ElementStore {

    private final Capabilities capabilities;
    private final ElementStore store;

    public CapabilitiesElementStore(Capabilities capabilities, ElementStore store) {
        if(capabilities == null){
            throw new NullPointerException("Capabilities must not be null!");
        }
        if(store == null){
            throw new NullPointerException("Store must not be null!");
        }
        this.capabilities = capabilities;
        this.store = store;
    }
    
    public static ElementStore valueOf(Capabilities capabilities, ElementStore store){
        if(store.getCapabilities().containsAll(capabilities)){
            return store;
        }else{
            return new CapabilitiesElementStore(capabilities, store);
        }
    }

    @Override
    public Capabilities getCapabilities() {
        return capabilities;
    }

    @Override
    public boolean load(Criteria criteria, Sorter sorter, ElementProcessor processor) throws StoreException {
        if(!capabilities.canRead()){
            throw new StoreException("Could not read from store!");
        }
        return store.load(criteria, sorter, processor);
    }

    @Override
    public long count(Criteria criteria) throws StoreException {
        if(!capabilities.canCount()){
            throw new StoreException("Could not count items in store!");
        }
        return store.count(criteria);
    }

    @Override
    public Element create(Element element) throws StoreException {
        if(!capabilities.canCreate()){
            throw new StoreException("Could not create item in store!");
        }
        return store.create(element);
    }

    @Override
    public long update(Criteria criteria, Element element) throws StoreException {
        if(!capabilities.canUpdate()){
            throw new StoreException("Could not update items in store!");
        }
        return store.update(criteria, element);
    }

    @Override
    public long remove(Criteria criteria) throws StoreException {
        if(!capabilities.canRemove()){
            throw new StoreException("Could not remove items in store!");
        }
        return store.remove(criteria);
    }

    @Override
    public void createAll(List<Element> elements) throws StoreException {
        if(!capabilities.canCreate()){
            throw new StoreException("Could not create items in store!");
        }
        store.createAll(elements);
    }    
}
