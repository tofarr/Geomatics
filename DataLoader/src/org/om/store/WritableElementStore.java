package org.om.store;

import java.util.List;
import org.om.criteria.Criteria;
import org.om.element.Element;

/**
 * Locking may be done on repository level
 * @author tofar
 */
public interface WritableElementStore extends ElementStore {

    Element create(Element element) throws StoreException;

    long update(Criteria criteria, Element element) throws StoreException;

    long remove(Criteria criteria) throws StoreException;

    void createAll(List<Element> elements) throws StoreException;

}
