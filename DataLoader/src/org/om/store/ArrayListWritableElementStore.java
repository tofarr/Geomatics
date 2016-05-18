package org.om.store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.om.criteria.Criteria;
import org.om.element.Element;
import org.om.sort.Sorter;

/**
 *
 * @author tofar
 */
public class ArrayListWritableElementStore implements WritableElementStore {

    private final Criteria validator;
    private volatile ArrayList<Element> elements;

    public ArrayListWritableElementStore(ArrayList<Element> elements, Criteria validator) {
        this.elements = elements;
        this.validator = validator;
    }

    @Override
    public Element create(Element element) throws StoreException {
        validate(element);
        synchronized (this) {
            ArrayList<Element> oldElements = elements;
            ArrayList<Element> newElements = new ArrayList<>(oldElements.size() + 1);
            newElements.addAll(oldElements);
            newElements.add(element);
            elements = newElements;
        }
        return element;
    }

    @Override
    public synchronized long update(Criteria criteria, Element updates) throws StoreException {
        synchronized (this) {
            ArrayList<Element> oldElements = elements;
            ArrayList<Element> newElements = new ArrayList<>(oldElements.size());
            int ret = 0;
            for (Element oldElement : oldElements) {
                if (criteria.match(oldElement)) {
                    Element element = (oldElement == null) ? updates : oldElement.merge(updates);
                    validate(element);
                    newElements.add(element);
                    ret++;
                } else {
                    newElements.add(oldElement);
                }
            }
            elements = newElements;
            return ret;
        }
    }

    @Override
    public synchronized long remove(Criteria criteria) throws StoreException {
        synchronized (this) {
            ArrayList<Element> oldElements = elements;
            ArrayList<Element> newElements = new ArrayList<>(oldElements.size());
            int ret = 0;
            for (Element oldElement : oldElements) {
                if (criteria.match(oldElement)) {
                    ret++;
                } else {
                    newElements.add(oldElement);
                }
            }
            elements = newElements;
            return ret;
        }
    }

    @Override
    public void createAll(List<Element> elements) throws StoreException {
        for(Element element : elements){
            if(!validator.match(element)){
                throw new StoreException("Element did not match criteria!");
            }
        }
        synchronized (this) {
            ArrayList<Element> oldElements = this.elements;
            ArrayList<Element> newElements = new ArrayList<>(oldElements.size() + elements.size());
            newElements.addAll(oldElements);
            newElements.addAll(elements);
            elements = newElements;
        }
    }

    @Override
    public boolean load(Criteria criteria, Sorter sorter, ElementProcessor processor) throws StoreException {
        ArrayList<Element> _elements = elements;
        if (sorter == null) {
            if (criteria == null) {
                for (Element element : _elements) {
                    if (!processor.process(element)) {
                        return false;
                    }
                }
            } else {
                for (Element element : _elements) {
                    if (criteria.match(element) && !processor.process(element)) {
                        return false;
                    }
                }
            }
            return true;
        }
        ArrayList<Element> ret = new ArrayList<>(_elements.size());
        if (criteria == null) {
            ret.addAll(_elements);
        } else {
            for (Element element : _elements) {
                if (criteria.match(element)) {
                    ret.add(element);
                }
            }
        }
        Collections.sort(ret, sorter);
        for (Element element : ret) {
            if (!processor.process(element)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public long count(Criteria criteria) throws StoreException {
        ArrayList<Element> _elements = elements;
        if (criteria == null) {
            return _elements.size();
        } else {
            int ret = 0;
            for (Element element : _elements) {
                if (criteria.match(element)) {
                    ret++;
                }
            }
            return ret;
        }
    }

    private void validate(Element element) {
        if((validator != null) && (!validator.match(element))){
            throw new StoreException("Element did not match criteria!");
        }
    }

}
