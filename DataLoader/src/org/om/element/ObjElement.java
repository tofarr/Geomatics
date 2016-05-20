package org.om.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.jayson.JaysonOutput;

/**
 *
 * @author tofar
 */
public class ObjElement extends Element implements Iterable<String> {

    public static final ObjElement EMPTY = new ObjElement(new HashMap<>());
    private final Map<String, Element> elements;

    ObjElement(Map<String, Element> elements) {
        this.elements = elements;
    }

    public static ObjElement valueOf(Map<String, Element> elements) {
        return elements.isEmpty() ? EMPTY : new ObjElement(new HashMap<>(elements));
    }

    public Map<String, Element> toMap() {
        return new HashMap<>(elements);
    }

    public Element getElement(String key) {
        return elements.get(key);
    }

    public boolean containsKey(String key) {
        return elements.containsKey(key);
    }

    public List<String> getCommonKeys(ObjElement other) {
        List<String> ret = new ArrayList<>();
        for (String key : elements.keySet()) {
            if (other.containsKey(key)) {
                ret.add(key);
            }
        }
        Collections.sort(ret);
        return ret;
    }

    @Override
    public ElementType getType() {
        return ElementType.OBJECT;
    }

    @Override
    public void toJson(JaysonOutput output) {
        output.beginObject();
        for (Entry<String, Element> entry : elements.entrySet()) {
            output.name(entry.getKey());
            entry.getValue().toJson(output);
        }
        output.endObject();
    }

    @Override
    public Iterator<String> iterator() {
        return Collections.unmodifiableMap(elements).keySet().iterator();
    }

    @Override
    public boolean matches(Element other) {
        if (other instanceof ObjElement) {
            ObjElement obj = (ObjElement) other;
            for (Entry<String, Element> entry : elements.entrySet()) {
                Element e = obj.elements.get(entry.getKey());
                if (!Element.match(entry.getValue(), e)) {
                    return false;
                }
            }
            for (Entry<String, Element> entry : obj.elements.entrySet()) {
                if ((!elements.containsKey(entry.getKey())) && (entry.getValue() != null)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public Element merge(Element updates) {
        if (updates instanceof ObjElement) {
            Map<String, Element> newElements = new HashMap<>(elements);
            newElements.putAll(((ObjElement) updates).elements);
            return new ObjElement(newElements);
        }
        return updates;
    }

    public ObjElement putElement(String key, Element element) {
        Map<String, Element> newElements = new HashMap<>(elements);
        newElements.put(key, element);
        return new ObjElement(newElements);
    }

}
