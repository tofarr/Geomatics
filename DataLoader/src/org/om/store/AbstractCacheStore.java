package org.om.store;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.om.attr.AttrSet;
import org.om.criteria.Criteria;
import org.om.element.ObjElement;
import org.om.sort.Sorter;

/**
 *
 * @author tofarrell
 */
public abstract class AbstractCacheStore implements ElementStore {

    private final Map<Criteria, ObjElement> cache;
    private final ElementStore store;

    public AbstractCacheStore(ElementStore store) {
        if (store == null) {
            throw new NullPointerException("Store must not be null!");
        }
        this.cache = new ConcurrentHashMap<>();
        this.store = store;
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
    public boolean load(final List<String> attrs, final Criteria criteria, Sorter sorter, final ElementProcessor processor) throws StoreException {
        
        if ((attrs != null) || (!isCachable(criteria))) { // query is not cachable, so bypass the cache
            return store.load(attrs, criteria, sorter, processor);
        }

        //try to get element from cache
        ObjElement element = cache.get(criteria);
        if (element != null) { // element was in cache - return it
            return processor.process(element);
        }

        //Populate cache with element
        return store.load(null, criteria, sorter, new ElementProcessor() {
            boolean done;

            @Override
            public boolean process(ObjElement element) {
                if (done) {
                    throw new StoreException("CacheStore Misconfiguration - multiple values found for single key!");
                }
                boolean ret = processor.process(element);
                cache.put(criteria, element);
                done = true;
                return ret;
            }
        });
    }

    @Override
    public long count(Criteria criteria) throws StoreException {
        return store.count(criteria);
    }

    @Override
    public ObjElement create(ObjElement element) throws StoreException {
        return store.create(element);
    }

    @Override
    public long update(Criteria criteria, ObjElement element) throws StoreException {
        long ret = store.update(criteria, element);
        if (isCachable(criteria)) {
            cache.remove(criteria);
        } else { // dunno what was updated - anything could be
            cache.clear();
        }
        return ret;
    }

    @Override
    public long remove(Criteria criteria) throws StoreException {
        long ret = store.remove(criteria);
        if (isCachable(criteria)) {
            cache.remove(criteria);
        } else { // dunno what was updated - anything could be
            cache.clear();
        }
        return ret;
    }

    @Override
    public void createAll(List<ObjElement> elements) throws StoreException {
        store.createAll(elements);
    }

    public abstract boolean isCachable(Criteria criteria);

}
