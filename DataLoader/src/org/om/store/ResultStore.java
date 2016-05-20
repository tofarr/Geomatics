package org.om.store;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.jayson.Jayson;
import org.jayson.JaysonBuffer;
import org.om.criteria.Criteria;
import org.om.element.Element;
import org.om.element.ObjElement;
import org.om.sort.Sorter;

/**
 *
 * @author tofar
 * @param <E>
 */
public class ResultStore<E> {

    private final Type type;
    private final Jayson jayson;
    private final ElementStore store;

    public ResultStore(Type type, Jayson jayson, ElementStore store) throws NullPointerException {
        if (type == null) {
            throw new NullPointerException("type must not be null");
        }
        if (jayson == null) {
            throw new NullPointerException("jayson must not be null");
        }
        if (store == null) {
            throw new NullPointerException("store must not be null");
        }
        this.type = type;
        this.jayson = jayson;
        this.store = store;
    }

    public Type getType() {
        return type;
    }

    public Jayson getJayson() {
        return jayson;
    }

    public ElementStore getStore() {
        return store;
    }

    public boolean load(Criteria criteria, Sorter sorter, final ObjProcessor<E> processor) throws StoreException {
        return store.load(null, criteria, sorter, new ElementProcessor() {

            final JaysonBuffer buffer = new JaysonBuffer();

            @Override
            public boolean process(ObjElement element) {
                E obj;
                if (element == null) {
                    obj = null;
                } else {
                    element.toJson(buffer.clear());
                    obj = (E) jayson.parse(type, buffer.getInput());
                }
                return processor.process(obj);
            }

        });
    }

    public long count(Criteria criteria) throws StoreException {
        return store.count(criteria);
    }

    public E create(E obj) throws StoreException {
        JaysonBuffer buffer = new JaysonBuffer();
        ObjElement element = toElement(obj, buffer);
        element = store.create(element);
        E ret = toObj(element, buffer);
        return ret;
    }

    public long update(Criteria criteria, E obj) throws StoreException {
        ObjElement element = toElement(obj, new JaysonBuffer());
        return store.update(criteria, element);
    }

    public long remove(Criteria criteria) throws StoreException {
        return store.remove(criteria);
    }

    public void createAll(List<E> obj) throws StoreException {
        List<ObjElement> elements = new ArrayList<>(obj.size());
        JaysonBuffer buffer = new JaysonBuffer();
        for (E e : obj) {
            elements.add(toElement(e, buffer));
        }
        store.createAll(elements);
        for (int i = 0; i < elements.size(); i++) {
            obj.set(i, toObj(elements.get(i), buffer));
        }
    }

    ObjElement toElement(E obj, JaysonBuffer buffer) {
        buffer.clear();
        getJayson().render(obj, getType(), buffer);
        Element element = Element.readJson(buffer.getInput());
        return (ObjElement)element;
    }

    E toObj(ObjElement element, JaysonBuffer buffer) {
        buffer.clear();
        element.toJson(buffer);
        E obj = (E) getJayson().parse(getType(), buffer.getInput());
        return obj;
    }

    public interface ObjProcessor<E> {

        boolean process(E obj);
    }
}
