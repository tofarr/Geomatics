package org.om.element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.jayson.JaysonOutput;

/**
 * Array of elements
 * @author tofar
 */
public final class ArrElement extends Element implements Iterable<Element> {

    public static final ArrElement EMPTY = new ArrElement(new Element[0]);
    private final Element[] values;
    
    ArrElement(Element[] values){
        this.values = values;
    }
    
    public static ArrElement valueOf(Element... values){
        return (values.length == 0) ? EMPTY : new ArrElement(values.clone());
    }
    
    public static ArrElement valueOf(List<Element> values){
        return values.isEmpty() ? EMPTY : new ArrElement(values.toArray(new Element[values.size()]));
    }
    
    @Override
    public ElementType getType() {
        return ElementType.ARRAY;
    }
    
    public int size(){
        return values.length;
    }
    
    public Element valueAt(int index){
        return values[index];
    }
    
    public List<Element> toList(){
        return new ArrayList<>(Arrays.asList(values));
    }
    
    public Element[] toArray(){
        return values.clone();
    }

    @Override
    public void toJson(JaysonOutput output) {
        output.beginArray();
        for(Element value : values){
            value.toJson(output);
        }
        output.endArray();
    }

    @Override
    public Iterator<Element> iterator() {
        return new Iterator<Element>(){
            int index;
            
            @Override
            public boolean hasNext() {
                return index < values.length;
            }

            @Override
            public Element next() {
                return values[index++];
            }
            
        };
    }

    @Override
    public boolean matches(Element other) {
        if(other instanceof ArrElement){
            ArrElement array = (ArrElement)other;
            if(values.length == array.values.length){
                for(int i = values.length; i-- > 0;){
                    if(!Element.match(values[i], array.values[i])){
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

}
