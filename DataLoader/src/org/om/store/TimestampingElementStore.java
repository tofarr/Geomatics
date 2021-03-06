package org.om.store;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.om.attr.AttrSet;
import org.om.criteria.Criteria;
import org.om.element.Element;
import org.om.element.NumElement;
import org.om.element.ObjElement;
import org.om.sort.Sorter;

/**
 * Store which attaches timestamps to elements. (Timestamps are doubles which should be ok for the
 * next 100000 years or so!)
 *
 * @author tofarrell
 */
public class TimestampingElementStore implements ElementStore {

    private final String createdAttr;
    private final String updatedAttr;
    private final ElementStore store;

    public TimestampingElementStore(String createdAttr, String updatedAttr, ElementStore store) {
        if (createdAttr == null && updatedAttr == null) {
            throw new NullPointerException("Either createdAttr or updatedAttr must be defined!");
        }
        if (store == null) {
            throw new NullPointerException("store must not be null");
        }
        this.createdAttr = createdAttr;
        this.updatedAttr = updatedAttr;
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
    public boolean load(List<String> attrs, Criteria criteria, Sorter sorter, ElementProcessor processor) throws StoreException {
        return store.load(attrs, criteria, sorter, processor);
    }

    @Override
    public long count(Criteria criteria) throws StoreException {
        return store.count(criteria);
    }

    @Override
    public ObjElement create(ObjElement element) throws StoreException {
        element = element.merge(createTimestamp());
        return store.create(element);
    }

    @Override
    public long update(Criteria criteria, ObjElement element) throws StoreException {
        if (updatedAttr != null) {
            //Check update attr on element
            checkUpdateAttr(criteria, element);
            //add update attr to element
            ObjElement obj = (ObjElement) element;
            NumElement timestamp = NumElement.valueOf((double)System.currentTimeMillis());
            element = obj.putElement(updatedAttr, timestamp);
        }
        return store.update(criteria, element);
    }

    @Override
    public long remove(Criteria criteria) throws StoreException {
        return store.remove(criteria);
    }

    @Override
    public void createAll(List<ObjElement> elements) throws StoreException {
        ObjElement timestamp = createTimestamp();
        for (int i = 0; i < elements.size(); i++) {
            ObjElement element = elements.get(i);
            element = element.merge(timestamp);
            elements.set(i, element);
        }
        store.createAll(elements);
    }

    private ObjElement createTimestamp() {
        long now = System.currentTimeMillis();
        Map<String, Element> attrs = new HashMap<>();
        if (createdAttr != null) {
            attrs.put(createdAttr, NumElement.valueOf(now));
        }
        if (updatedAttr != null) {
            attrs.put(updatedAttr, NumElement.valueOf(now));
        }
        ObjElement ret = ObjElement.valueOf(attrs);
        return ret;
    }

    private void checkUpdateAttr(Criteria criteria, Element element) {
        ObjElement obj = (ObjElement) element;
        Element updated = obj.getElement(updatedAttr);
        if ((updated != null) && (updated instanceof NumElement)) {
            final double newLastUpdate = ((NumElement) updated).getNum();
            if (newLastUpdate > 0) {
                load(Arrays.asList(updatedAttr), criteria, null, new ElementProcessor() {
                    @Override
                    public boolean process(ObjElement element) {
                        Element updated = element.getElement(updatedAttr);
                        if ((updated != null) && (updated instanceof NumElement)) {
                            double oldLastUpdate = ((NumElement) updated).getNum();
                            if (oldLastUpdate > newLastUpdate) { // chances are we have a conflict
                                throw new StoreException("Found element newer than timestamp provided!");
                            }
                        }
                        return true;
                    }
                });
            }
        }
    }
}
