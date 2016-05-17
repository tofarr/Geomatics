package org.om.sort;

import java.beans.ConstructorProperties;
import org.om.element.ArrElement;
import org.om.element.Element;
import org.om.element.ObjElement;

/**
 *
 * @author tofarrell
 */
public class AttrSorter implements Sorter {

    private final String attrName;
    private final Sorter order;
    private transient Integer index;

    @ConstructorProperties({"attrName", "order"})
    public AttrSorter(String attrName, Sorter order) {
        this.attrName = attrName;
        this.order = order;
    }

    public String getAttrName() {
        return attrName;
    }

    public Sorter getOrder() {
        return order;
    }

    @Override
    public int compare(Element o1, Element o2) {
        o1 = getAttr(o1);
        o2 = getAttr(o2);
        return order.compare(o1, o2);
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
