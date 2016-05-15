package org.lcd.criteria;

import java.beans.ConstructorProperties;
import org.lcd.Result;
import org.lcd.ResultIterator;

/**
 *
 * @author tofar
 */
public class Equal<E> implements Criteria{

    protected final String attrName;
    protected final E value;

    @ConstructorProperties({"attrName","value"})
    public Equal(String attrName, E value) {
        if(attrName.isEmpty()){
            throw new IllegalArgumentException("attrName must not be empty!");
        }
        this.attrName = attrName;
        this.value = value;
    }

    public String getAttrName() {
        return attrName;
    }

    public E getValue() {
        return value;
    }

    @Override
    public boolean match(Result result) {
        return matchValue(result.getByName(attrName));
    }

    @Override
    public boolean matchCurrent(ResultIterator iter) {
        return matchValue(iter.getByName(attrName));
    }
    
    
    protected boolean matchValue(Object value){
        if(value == null){
            return (this.value == null);
        }else if(this.value == null){
            return false;
        }else if(this.value.getClass() == value.getClass()){
            return this.value.equals(value);
        }else if(value instanceof Number){
            if(this.value instanceof Number){
                return ((Number)this.value).doubleValue() == ((Number)value).doubleValue();
            }else{
                try{
                    double v = Double.parseDouble(this.value.toString());
                    return v == ((Number)value).doubleValue();
                }catch(NumberFormatException ex){
                    return false;
                }    
            }                
        }else if(this.value instanceof Number){
            try{
                double v = Double.parseDouble(value.toString());
                return v == ((Number)this.value).doubleValue();
            }catch(NumberFormatException ex){
                return false;
            }
        }else{
            return value.toString().equals(value.toString());
        }
    }
}
