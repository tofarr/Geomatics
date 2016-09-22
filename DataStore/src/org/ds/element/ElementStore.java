package org.ds.element;

import java.util.List;
import org.ds.Processor;
import org.ds.Store;
import org.ds.StoreException;
import org.roa.attr.Attr;
import org.roa.attr.AttrSet;
import org.roa.criteria.Criteria;
import org.ds.element.model.ObjElement;
import org.ds.element.sorter.Sorter;

/**
 *
 * @author tofarr
 * @param <K>
 */
public interface ElementStore<K> extends Store<K, ObjElement> {

    AttrSet getAttrs();
    
    Attr getKeyAttr();
    
    boolean load(List<String> attrs, Criteria criteria, Sorter sorter, Processor<ObjElement> processor) throws StoreException;
       
    long count(Criteria criteria) throws StoreException;
    
    long update(Criteria criteria, ObjElement element) throws StoreException;

    long remove(Criteria criteria) throws StoreException;

    void createAll(List<ObjElement> elements) throws StoreException;

}
