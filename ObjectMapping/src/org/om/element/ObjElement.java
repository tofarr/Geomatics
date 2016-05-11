package org.om.element;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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

    @Override
    public ElementType getType() {
        return ElementType.OBJECT;
    }

    @Override
    public void toJson(JaysonOutput output) {
        output.beginObject();
        for(Entry<String,Element> entry : elements.entrySet()){
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
        if(other instanceof ObjElement){
            ObjElement obj = (ObjElement)other;
            for(Entry<String,Element> entry : elements.entrySet()){
                Element e = obj.elements.get(entry.getKey());
                if(!Element.match(entry.getValue(), e)){
                    return false;
                }
            }
            for(Entry<String,Element> entry : obj.elements.entrySet()){
                if((!elements.containsKey(entry.getKey())) && (entry.getValue() != null)){
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
