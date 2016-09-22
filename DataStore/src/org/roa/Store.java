package org.roa;

import org.roa.criteria.Criteria;
import org.ds.element.sorter.Sorter;

/**
 * 
 * @author tofarr
 * @param <V>
 */
public interface Store<V> extends Resource {
    
    StoreSpec getSpec();

    void add(long id, V value) throws ResourceException;

    Entry<V> add(V value) throws ResourceException;

    Entry<V> get(long id) throws ResourceException;
    
    /** 
     * Available with permission get, but may be cheaper in some instances 
     * @param id
     * @return 
     */
    boolean exists(long id) throws ResourceException;

    boolean update(long id, V value) throws ResourceException;

    long update(Criteria criteria, V value) throws ResourceException;

    boolean remove(long id) throws ResourceException;

    long remove(Criteria criteria);

    boolean iterate(Processor<Entry<V>> processor) throws ResourceException;

    boolean iterate(Criteria criteria, Sorter sorter, Processor<Entry<V>> processor) throws ResourceException;

    long count() throws ResourceException;

    long count(Criteria criteria) throws ResourceException;
    
}
