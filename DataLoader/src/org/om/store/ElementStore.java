package org.om.store;

import java.util.List;
import org.om.attr.AttrSet;
import org.om.criteria.Criteria;
import org.om.element.ObjElement;
import org.om.sort.Sorter;

/**
 *
 * @author tofar
 */
public interface ElementStore {
    
    /*
    should everything be an ObjElement? can we think of a good reason why this would not be the case?
    
    think about locking, temporal, spatial and security
    like it or not, security is part of this.
    I think for a start we need a capabilities object and replace writable        
    ok walk through how capabilities will work.
    
    stored item should always be an OBJ
    
    id store - stores an id on each OBJ - should all stores have this?
    
    only question remains - should we build in schemas? Add id and attrs?
    
    (can build on top)
    cachablestore - stores and updates a timestamp on each element - checks timestamp is correct on update. (or not present)
    
    (can build on top)
    lockable store - has locks on each element, can be set to READ, WRITE, and UPDATE with an id.
    
    (can build on top)
    entitlement store - has group ids with read and write permissions per item, and for whole store
    
    (can build on top)
    trigger store - runs trigger on operation (can build on top of existing framework)
    
    (can build on top)
    cache store - store items in memory before passing on to store
    
    
   */      
    
    public Capabilities getCapabilities();
    
    AttrSet getAttrs();
    
    boolean load(List<String> attrs, Criteria criteria, Sorter sorter, ElementProcessor processor) throws StoreException;
       
    long count(Criteria criteria) throws StoreException;

    ObjElement create(ObjElement element) throws StoreException;

    long update(Criteria criteria, ObjElement element) throws StoreException;

    long remove(Criteria criteria) throws StoreException;

    void createAll(List<ObjElement> elements) throws StoreException;
}
