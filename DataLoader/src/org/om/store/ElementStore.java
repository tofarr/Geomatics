package org.om.store;

import org.om.criteria.Criteria;
import org.om.element.Element;
import org.om.sort.Sorter;

/**
 *
 * @author tofar
 */
public interface ElementStore {

    boolean load(Criteria criteria, Sorter sorter, ElementProcessor processor) throws StoreException;
    
    long count(Criteria criteria) throws StoreException;

    interface ElementProcessor {

        boolean process(Element element);
    }
}
