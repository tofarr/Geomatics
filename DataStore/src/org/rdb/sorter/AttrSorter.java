package org.rdb.sorter;

import java.beans.ConstructorProperties;
import org.jayson.element.ArrElement;
import org.jayson.element.Element;
import org.jayson.element.ObjElement;


/**
 *
 * @author tofarrell
 */
public class AttrSorter implements Sorter {

    private final String attrName;
    private final Sorter sorter;
    private transient Integer index;

    @ConstructorProperties({"attrName", "sorter"})
    public AttrSorter(String attrName, Sorter sorter) {
        this.attrName = attrName;
        this.sorter = sorter;
    }

    public String getAttrName() {
        return attrName;
    }

    public Sorter getSorter() {
        return sorter;
    }

    @Override
    public int compare(Element o1, Element o2) {
        o1 = getAttr(o1);
        o2 = getAttr(o2);
        return sorter.compare(o1, o2);
    }
    
    private Integer getIndex(){
        if(index != null){
            return index;
        }
        try{
            index = Integer.parseInt(attrName);
        }catch(NumberFormatException ex){
            index = -1;
        }
        return index;
    }

    private Element getAttr(Element e) {
        if(e == null){
            return null;
        }
        switch(e.getType()){
            case ARRAY:
                ArrElement arr = (ArrElement)e;
                Integer index = getIndex();
                if((index < 0) || index >= arr.size()){
                    return null;
                }
                return arr.valueAt(index);
            case OBJECT:
                return ((ObjElement)e).getElement(attrName);
            default:
                return null;
        }
    }
    
    
}
