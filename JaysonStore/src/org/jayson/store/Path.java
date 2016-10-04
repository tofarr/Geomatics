
package org.jayson.store;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.jayson.element.ArrElement;
import org.jayson.element.Element;
import org.jayson.element.ObjElement;
import org.jayson.element.ObjElementBuilder;

/**
 *
 * @author tofarr
 */
public class Path {
    
    public static final Path EMPTY = new Path(new String[0]);
    public static final String WILD = "*";
    public static final Pattern ELEMENT = Pattern.compile("\\w+");
    public static final Pattern DELIMITER = Pattern.compile("/");
    private final String[] elements;

    @ConstructorProperties({"elements"})
    public Path(String[] elements) throws NullPointerException, IllegalArgumentException {
        check(elements);
        this.elements = elements.clone();
    }
    
    public Path(String string){
        this.elements = DELIMITER.split(string);
        check(elements);
    }
    
    static void check(String[] elements){
        for(int e = 0; e < elements.length; e++){
            String key = elements[e];
            if((!WILD.equals(key)) && (!ELEMENT.matcher(key).matches())){
                throw new IllegalArgumentException("Invalid element '"+key+"' at index "+e+" of path '"+String.join(DELIMITER.pattern(), elements)+"'");
            }
        }
    }

    public String[] getElements() {
        return elements.clone();
    }
    
    public String get(int index) throws IndexOutOfBoundsException {
        return elements[index];
    }
    
    public int length(){
        return elements.length;
    }
    
    public boolean beginsWith(Path other){
        if(elements.length < other.elements.length){
            return false;
        }
        for(int i = 0; i < other.elements.length; i++){   
            if(!(WILD.equals(elements[i])
                    || WILD.equals(other.elements[i]) 
                    || elements[i].equals(other.elements[i]))){
                return false;
            }
        }
        return true;
    }
    
    public boolean process(Element element, ResultProcessor processor){
        return process(0, element, processor);
    }
    
    public boolean process(int pathIndex, Element element, ResultProcessor processor){
        if(element != null){
            if(pathIndex == elements.length){ 
                 return processor.process(element);
            }
            String key = elements[pathIndex];
            if(element instanceof ObjElement){
                ObjElement parent = (ObjElement)element;
                if(WILD.equals(key)){
                    pathIndex++;
                    for(String k : parent){
                        if(!process(pathIndex, parent.getElement(k), processor)){
                            return false;
                        }
                    }
                }else if(!process(pathIndex, parent.getElement(key), processor)){
                    return false;
                }
            }else if(element instanceof ArrElement){
                if(WILD.equals(key)){
                    
                }
                try{
                    int index = Integer.parseInt(key);
                    ArrElement parent = (ArrElement)element;
                    element = (parent.size() <= index) ? null : parent.valueAt(index);
                }catch(NumberFormatException ex){
                    throw new PathException("Not an array index : " + key);
                }
            }
        }
        return true;
    }
    
    public Element updateElement(Element root, Element newValue) throws PathException{
       return updateElement(0, root, newValue);
    }
    
    private Element updateElement(int index, Element element, Element newValue) throws PathException{
        if(index == elements.length){
            return newValue;
        }else{
            String key = elements[index];
            index++;
            if(element == null){
                Element child = updateElement(index, null, newValue);
                Element ret = new ObjElementBuilder().put(key, child).build();
                return ret;
            }else if(element instanceof ObjElement){
                ObjElement parent = (ObjElement)element;
                if(WILD.equals(key)){
                    ObjElementBuilder builder = new ObjElementBuilder();
                    for(String k : parent){
                        builder.put(k, updateElement(index, parent.getElement(k), newValue));
                    }
                    return builder.build();
                }else{
                    Element child = updateElement(index, parent.getElement(key), newValue);
                    parent = parent.toBuilder().put(key, child).build();
                    return parent;
                }                
            }else if(element instanceof ArrElement){
                ArrElement parent = (ArrElement)element;
                if(WILD.equals(key)){
                    ArrayList<Element> children = new ArrayList<>();
                    for(int i = 0; i < parent.size(); i++){
                        children.add(updateElement(index, children.get(i), newValue));
                    }
                    return ArrElement.valueOf(children);
                }else{
                    try{
                        int arrayIndex = Integer.parseInt(key);
                        List<Element> children = parent.toList();
                        Element child = (arrayIndex < children.size()) ? children.get(arrayIndex) : null;
                        child = updateElement(index, child, newValue);
                        if(arrayIndex == children.size()){
                            children.add(child);
                        }else{
                            children.set(arrayIndex, element);
                        }
                        return ArrElement.valueOf(children);
                    }catch(NumberFormatException ex){
                        throw new PathException("Not an array index : " + key);
                    }
                }
            }else{
                throw new PathException("Invalid path : "+this+" within : "+element);
            }
        }
    }
    
    public Path add(String child){
        String[] newElements = Arrays.copyOf(elements, elements.length+1);
        newElements[elements.length] = child;
        return new Path(newElements);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(this.elements);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Path) && Arrays.equals(((Path)obj).elements, elements);
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < elements.length; i++){
            if(i > 0){
                str.append('/');
            }
            str.append(elements[i]);
        }
        return str.toString();
    }
}
