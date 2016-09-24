package org.roa;

import org.roa.criteria.Criteria;
import org.roa.sorter.Sorter;

/**
 * 
 * @author tofarr
 * @param <V>
 */
public interface Store<V> {
    
    StoreInfo getInfo();
    
    Workspace getGroup();
     
    long getLastModified();

    String getETag();  
    
    void lock(SecurityContext context, long id, long timeout) throws ResourceException;
    
    void unlock(SecurityContext context, long id) throws ResourceException;

    void add(SecurityContext context, long id, V value) throws ResourceException;

    Entry<V> add(SecurityContext context, V value) throws ResourceException;

    Entry<V> get(SecurityContext context, long id) throws ResourceException;
    
    /** 
     * Available with permission get, but may be cheaper in some instances 
     * @param context
     * @param id
     * @return 
     */
    boolean exists(SecurityContext context, long id) throws ResourceException;

    boolean update(SecurityContext context, long id, V value) throws ResourceException;

    long update(SecurityContext context, Criteria criteria, V value) throws ResourceException;

    boolean remove(SecurityContext context, long id) throws ResourceException;

    long remove(SecurityContext context, Criteria criteria);

    boolean query(SecurityContext context, Processor<Entry<V>> processor) throws ResourceException;

    boolean query(SecurityContext context, Criteria criteria, Sorter sorter, Processor<Entry<V>> processor) throws ResourceException;

    long count(SecurityContext context) throws ResourceException;

    long count(SecurityContext context, Criteria criteria) throws ResourceException; 
}
