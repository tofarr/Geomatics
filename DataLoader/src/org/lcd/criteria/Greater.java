
package org.lcd.criteria;

import java.beans.ConstructorProperties;

/**
 *
 * @author tofar
 */
public class Greater<C extends Comparable> extends Equal<C>{

    @ConstructorProperties({"attrName","value"})
    public Greater(String attrName, C value) {
        super(attrName, value);
    }    
    
    @Override
    protected boolean matchValue(Object value){
        if(value == null){
            return (this.value == null);
        }else if(this.value == null){
            return false;
        }else if(this.value.getClass() == value.getClass()){
            return this.value.compareTo(value) > 0;
        }else if(value instanceof Number){
            if(this.value instanceof Number){
                return ((Number)value).doubleValue() > ((Number)this.value).doubleValue();
            }else{
                try{
                    double v = Double.parseDouble(this.value.toString());
                    return ((Number)value).doubleValue() > v;
                }catch(NumberFormatException ex){
                    return false;
                }    
            }                
        }else if(this.value instanceof Number){
            try{
                double v = Double.parseDouble(value.toString());
                return v > ((Number)this.value).doubleValue();
            }catch(NumberFormatException ex){
                return false;
            }
        }else{
            return value.toString().compareTo(value.toString()) > 0;
        }
    }
}
