package org.rdb;

import java.beans.ConstructorProperties;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 *
 * @author tofarr
 */
public final class NodePath {

    public static final Pattern ELEMENT = Pattern.compile("\\w+");
    public static final Pattern DELIMITER = Pattern.compile("/");
    private final String[] elements;

    @ConstructorProperties({"elements"})
    public NodePath(String[] elements) throws NullPointerException, IllegalArgumentException {
        check(elements);
        this.elements = elements.clone();
    }
    
    public NodePath(String string){
        this.elements = DELIMITER.split(string);
        check(elements);
    }
    
    static void check(String[] elements){
        for(int e = 0; e < elements.length; e++){
            String element = elements[e];
            if(!ELEMENT.matcher(element).matches()){
                throw new IllegalArgumentException("Invalid element '"+element+"' at index "+e+" of path '"+String.join(DELIMITER.pattern(), elements)+"'");
            }
        }
    }

    public String[] getElements() {
        return elements.clone();
    }
    
    public String getElement(int index) throws IndexOutOfBoundsException {
        return elements[index];
    }
    
    public int length(){
        return elements.length;
    }
    
    public NodePath add(String child){
        String[] newElements = Arrays.copyOf(elements, elements.length+1);
        newElements[elements.length] = child;
        return new NodePath(newElements);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(this.elements);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof NodePath) && Arrays.equals(((NodePath)obj).elements, elements);
    }
    
    
}
