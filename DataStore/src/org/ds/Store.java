package org.ds;

import org.ds.cache.CacheSettings;

/**
 *
 * @author tofarr
 */
public interface Store<K, V> {
    
    String getName();

    CacheSettings getCacheSettings();
    
    Capabilities getCapabilities();
    
    V get(K key) throws StoreException;
    
    boolean load(Processor<V> processor) throws StoreException;
    
    long count() throws StoreException;
    
    V create(V value) throws StoreException;
    
    //V update(K key, V value);
    
    boolean remove(K key) throws StoreException;
    
}
