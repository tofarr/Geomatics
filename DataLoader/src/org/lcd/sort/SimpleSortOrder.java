package org.lcd.sort;

import java.beans.ConstructorProperties;
import org.lcd.AttrSet;
import org.lcd.Result;

/**
 *
 * @author tofar
 */
public class SimpleSortOrder implements SortOrder{

    private final String attrName;

    @ConstructorProperties({"attrName"})
    public SimpleSortOrder(String attrName) {
        this.attrName = attrName;
    }

    public String getAttrName() {
        return attrName;
    }

    @Override
    public int compare(Result r1, Result r2) {
        return compareValues(r1.getByName(attrName), r2.getByName(attrName));
    }

    @Override
    public int compare(AttrSet attrs, Object[] valuesA, Object[] valuesB) {
        int index = attrs.indexOf(attrName);
        return compareValues(valuesA[index], valuesB[index]);
    }

    public static int compareValues(Object o1, Object o2) {
        if(o1 == null){
            return (o2 == null) ? 0 : 1;
        }else if(o2 == null){
            return -1;
        }else if((o1.getClass() == o2.getClass()) && Comparable.class.isAssignableFrom(o1.getClass())){
            return ((Comparable)o1).compareTo(o2);
        }else if(Number.class.isAssignableFrom(o1.getClass())){
            double v1 = ((Number)o1).doubleValue();
            if(Number.class.isAssignableFrom(o2.getClass())){
                return Double.compare(v1, ((Number)o2).doubleValue());
            }else{
                try{
                    double v2 = Double.parseDouble(o2.toString());
                    return Double.compare(v1, v2);
                }catch(NumberFormatException ex){
                    return -1; // numbers first
                } 
            }
        }else if(Number.class.isAssignableFrom(o2.getClass())){
            try{
                double v1 = Double.parseDouble(o1.toString());
                return Double.compare(v1, ((Number)o2).doubleValue());
            }catch(NumberFormatException ex){
                return 1; // numbers first
            } 
        }
        return o1.toString().compareTo(o2.toString());
    }
}
