package org.roa.sorter;

import org.roa.element.ArrElement;
import org.roa.element.Element;
import org.roa.element.ElementType;
import org.roa.element.ObjElement;
import org.roa.element.ValElement;
import org.jayson.parser.StaticFactory;

/**
 *
 * @author tofarrell
 */
public class ValueSorter implements Sorter {

    public static final ValueSorter INSTANCE = new ValueSorter();

    private ValueSorter() {
    }

    @StaticFactory({})
    public static ValueSorter getInstance() {
        return INSTANCE;
    }

    @Override
    public int compare(Element o1, Element o2) {
        if(o1 == null){
            return (o2 == null) ? 0 : 1; // nulls last
        }else if(o2 == null){
            return -1; // nulls last
        }else if(o1 == o2){
            return 0;
        }
        switch(o1.getType()){
            case ARRAY:
                return compareArrTo((ArrElement)o1, o2);
            case OBJECT:
                return compareObjTo((ObjElement)o1, o2);
            default:
                return compareValTo((ValElement)o1, o2);
        }
    }

    private int compareArrTo(ArrElement arr, Element e) {
        switch(e.getType()){
            case ARRAY:
                ArrElement other = (ArrElement)e;
                int s = Math.min(arr.size(), other.size());
                for(int i = 0; i < s; i++){ // compare elements
                    int ret = compare(arr.valueAt(i), other.valueAt(i));
                    if(ret != 0){
                        return ret;
                    }
                }
                return Integer.compare(arr.size(), other.size()); // shorter arrays first
            case OBJECT:
                return -1; // Array before object
            default:
                return 1; // Array after value
        }
    }
    
    private int compareObjTo(ObjElement obj, Element e) {
        if(e.getType() != ElementType.OBJECT){
            return 1; // everything before object
        }
        ObjElement other = (ObjElement)e;
        for(String key : obj.getCommonKeys(other)){
            Element v1 = obj.getElement(key);
            Element v2 = other.getElement(key);
            int ret = compare(v1, v2);
            if(ret != 0){
                return ret;
            }
        }
        return 0;
    }

    private int compareValTo(ValElement val, Element e) {
        switch(e.getType()){
            case ARRAY:
            case OBJECT:
                return -1;
            default:
                ValElement other = (ValElement)e;
                try{
                    return Double.compare(val.asNum().getNum(), other.asNum().getNum());
                }catch(NumberFormatException ex){
                    return val.asStr().getStr().compareTo(other.asStr().getStr());
                }
        }
    }

}
