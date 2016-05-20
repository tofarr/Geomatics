package org.om.store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.om.attr.AttrSet;
import org.om.criteria.Criteria;
import org.om.element.ObjElement;
import org.om.sort.Sorter;

/**
 *
 * @author tofar
 */
public class ArrayListElementStore implements ElementStore {

    private final AttrSet attrs;
    private final boolean allowUnknowns;
    private volatile ArrayList<ObjElement> elements;
    

    public ArrayListElementStore(AttrSet attrs, boolean allowUnknowns, ArrayList<ObjElement> elements) {
        this.attrs = attrs;
        this.allowUnknowns = allowUnknowns;
        this.elements = elements;
    }

    public ArrayListElementStore(AttrSet attrs) {
        this.attrs = attrs;
        this.allowUnknowns = true;
        this.elements = new ArrayList<>();
    }

    @Override
    public Capabilities getCapabilities() {
        return Capabilities.ALL;
    }

    @Override
    public AttrSet getAttrs() {
        return attrs;
    }

    @Override
    public synchronized ObjElement create(ObjElement element) throws StoreException {
        attrs.validate(element, allowUnknowns);
        ArrayList<ObjElement> oldElements = elements;
        ArrayList<ObjElement> newElements = new ArrayList<>(oldElements.size() + 1);
        newElements.addAll(oldElements);
        newElements.add(element);
        elements = newElements;
        return element;
    }

    @Override
    public synchronized long update(Criteria criteria, ObjElement updates) throws StoreException {
        ArrayList<ObjElement> oldElements = elements;
        ArrayList<ObjElement> newElements = new ArrayList<>(oldElements.size());
        int ret = 0;
        for (ObjElement oldElement : oldElements) {
            if (criteria.match(oldElement)) {
                ObjElement element = (oldElement == null) ? updates : oldElement.merge(updates);
                attrs.validate(element, allowUnknowns);
                newElements.add(element);
                ret++;
            } else {
                newElements.add(oldElement);
            }
        }
        elements = newElements;
        return ret;
    }

    @Override
    public synchronized long remove(Criteria criteria) throws StoreException {
            ArrayList<ObjElement> oldElements = elements;
            ArrayList<ObjElement> newElements = new ArrayList<>(oldElements.size());
            int ret = 0;
            for (ObjElement oldElement : oldElements) {
                if (criteria.match(oldElement)) {
                    ret++;
                } else {
                    newElements.add(oldElement);
                }
            }
            elements = newElements;
            return ret;
    }

    @Override
    public synchronized void createAll(List<ObjElement> elements) throws StoreException {
        ArrayList<ObjElement> oldElements = this.elements;
        ArrayList<ObjElement> newElements = new ArrayList<>(oldElements.size() + elements.size());
        newElements.addAll(oldElements);
        for(ObjElement element : elements){
            attrs.validate(element, allowUnknowns);
            newElements.add(element);
        }
        elements = newElements;
    }

    @Override
    public boolean load(List<String> attrs, Criteria criteria, Sorter sorter, ElementProcessor processor) throws StoreException {
        AttrSet attrsToLoad = (attrs == null) ? this.attrs : this.attrs.filter(attrs);
        ArrayList<ObjElement> _elements = elements;
        if(sorter == null){
            for (ObjElement element : _elements) {
                if(!criteria.match(element)){
                    continue;
                }
                if(this.attrs != attrsToLoad){
                    element = attrsToLoad.filterElement(element);
                }
                if (processor.process(element)) {
                    return false;
                }
            }
        }
        
        ArrayList<ObjElement> ret = new ArrayList<>(_elements.size());
        for (ObjElement element : _elements) {
            if(!criteria.match(element)){
                continue;
            }
            if(this.attrs != attrsToLoad){
                element = attrsToLoad.filterElement(element);
            }
            ret.add(element);
        }
        Collections.sort(ret, sorter);
        for (ObjElement element : ret) {
            if (!processor.process(element)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public long count(Criteria criteria) throws StoreException {
        ArrayList<ObjElement> _elements = elements;
        if (criteria == null) {
            return _elements.size();
        } else {
            int ret = 0;
            for (ObjElement element : _elements) {
                if (criteria.match(element)) {
                    ret++;
                }
            }
            return ret;
        }
    }
}
