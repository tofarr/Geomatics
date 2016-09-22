package org.ds.element.model;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jayson.JaysonInput;
import org.jayson.JaysonOutput;
import org.jayson.JaysonReader;
import org.jayson.JaysonType;
import org.jayson.JaysonWriter;
import org.jayson.PrettyPrintJaysonWriter;

/**
 *
 * @author tofar
 */
public abstract class Element {

    Element() {
    }
    
    public String toString(){
        return toString(true);
    }
    
    public String toString(boolean prettyPrint){
        StringBuilder str = new StringBuilder();
        toStream(prettyPrint, str);
        return str.toString();
    }
    
    public void toStream(boolean prettyPrint, Appendable appendable){
        JaysonOutput output = prettyPrint ? new PrettyPrintJaysonWriter(appendable)
                : new JaysonWriter(appendable);
        toJson(output);
    }
        
    public abstract void toJson(JaysonOutput output);

    public abstract ElementType getType();
    
    public abstract boolean matches(Element other);
    
    public static Element readString(String str){
        return readStream(new StringReader(str));
    }
    
    public static Element readStream(Reader reader){
        return readJson(new JaysonReader(reader));
    }
    
    public static Element readJson(JaysonInput input){
        JaysonType type = input.next();
        return readJson(type, input);
    }
    
    public static Element readJson(JaysonType type, JaysonInput input){
        if(type == null){
            return null;
        }
        switch(type){
            case BEGIN_ARRAY:
                List<Element> elements = new ArrayList<>();
                while(true){
                    type = input.next();
                    if(type == JaysonType.END_ARRAY){
                        break;
                    }
                    elements.add(readJson(type, input));
                }
                return ArrElement.valueOf(elements);
            case BEGIN_OBJECT:
                Map<String,Element> map = new HashMap<>();
                while(true){
                    type = input.next();
                    if(type == JaysonType.END_OBJECT){
                        break;
                    }
                    String key = input.str();
                    Element element = readJson(input);
                    map.put(key, element);
                }
                return map.isEmpty() ? ObjElement.EMPTY : new ObjElement(map);
            case BOOLEAN:
                return BoolElement.valueOf(input.bool());
            case NULL:
                return null;
            case NUMBER:
                return NumElement.valueOf(input.num());
            case STRING:
                return StrElement.valueOf(input.str());
            default:
                throw new IllegalStateException("Encountered unexpected type : "+type);
        }
    }
    
    public static boolean match(Element a, Element b){
        if(a == null){
            return (b == null);
        }else{
            return a.matches(b);
        }
    }

    public Element merge(Element updates) {
        return updates;
    }
}
