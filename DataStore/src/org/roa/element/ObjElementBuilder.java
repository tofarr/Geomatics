package org.roa.element;

import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author tofar
 */
public class ObjElementBuilder implements Iterable<String> {

    private HashMap<String, Element> elements;
    private boolean copyOnEdit;

    public ObjElementBuilder() {
        elements = new HashMap<>();
    }

    public ObjElement build(){
        copyOnEdit = true;
        return new ObjElement(elements);
    }
    
    public Element get(String key){
        return elements.get(key);
    }
    
    public ObjElementBuilder put(String key, Element element){
        if(copyOnEdit){
            elements = new HashMap<>(elements);
            copyOnEdit = false;
        }
        elements.put(key, element);
        return this;
    }
    
    public ObjElementBuilder putStr(String key, String str){
        return put(key, StrElement.valueOf(str));
    }
    
    public ObjElementBuilder putNum(String key, double num){
        return put(key, NumElement.valueOf(num));
    }
    
    public ObjElementBuilder putBool(String key, boolean bool){
        return put(key, BoolElement.valueOf(bool));
    }

    @Override
    public Iterator<String> iterator() {
        return new Iterator<String>(){
            final String[] keys = elements.keySet().toArray(new String[elements.size()]);
            int index;
            
            @Override
            public boolean hasNext() {
                return (index < keys.length);
            }

            @Override
            public String next() {
                return keys[index++];
            }
            
        };
    }
    
}
