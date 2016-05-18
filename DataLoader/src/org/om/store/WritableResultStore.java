package org.om.store;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.jayson.Jayson;
import org.jayson.JaysonBuffer;
import org.om.criteria.Criteria;
import org.om.element.Element;

/**
 *
 * @author tofar
 * @param <E>
 */
public class WritableResultStore<E> extends ResultStore<E> {

    private final WritableElementStore store;

    public WritableResultStore(Type type, Jayson jayson, WritableElementStore store) throws NullPointerException {
        super(type, jayson, store);
        this.store = store;
    }

    public Element create(E obj) throws StoreException {
        return store.create(toElement(obj, new JaysonBuffer()));
    }

    public long update(Criteria criteria, E obj) throws StoreException {
        return store.update(criteria, toElement(obj, new JaysonBuffer()));
    }

    public long remove(Criteria criteria) throws StoreException {
        return store.remove(criteria);
    }

    public void createAll(List<E> obj) throws StoreException {
        List<Element> elements = new ArrayList<>(obj.size());
        JaysonBuffer buffer = new JaysonBuffer();
        for(E e : obj){
            elements.add(toElement(e, buffer));
        }
        store.createAll(elements);
        for(int i = 0; i < elements.size(); i++){
            obj.set(i, toObj(elements.get(i), buffer));
        }
    }
    
    Element toElement(E obj, JaysonBuffer buffer){
        if(obj == null){
            return null;
        }
        getJayson().render(obj, getType(), buffer);
        Element element = Element.readJson(buffer.getInput());
        return element;
    }
    
    E toObj(Element element, JaysonBuffer buffer){
        if(element == null){
            return null;
        }
        buffer.clear();
        element.toJson(buffer);
        E obj = (E)getJayson().parse(getType(), buffer.getInput());
        return obj;
    }

}
