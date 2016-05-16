package org.lcd.criteria;

import java.beans.ConstructorProperties;

/**
 *
 * @author tofar
 */
public class Equal<E> implements Criteria<E>{

    private final E value;

    @ConstructorProperties({"value"})
    public Equal(E value) {
        this.value = value;
    }

    public final E getValue() {
        return value;
    }

    @Override
    public boolean match(E value){
        if(this.value == null){
            return (value == null);
        }else if(value == null){
            return false;
        }else if(this.value.getClass() == value.getClass()){
            return this.value.equals(value);
        }else if(Number.class.isAssignableFrom(this.value.getClass())){
            double v1 = ((Number)this.value).doubleValue();
            if(Number.class.isAssignableFrom(value.getClass())){
                return v1 == ((Number)value).doubleValue();
            }else{
                try{
                    double v2 = Double.parseDouble(value.toString());
                    return v1 == v2;
                }catch(NumberFormatException ex){
                    return false;
                } 
            }
        }else if(Number.class.isAssignableFrom(value.getClass())){
            try{
                double v1 = Double.parseDouble(this.value.toString());
                return (v1 == ((Number)value).doubleValue());
            }catch(NumberFormatException ex){
                return false; // numbers first
            } 
        }
        return this.value.toString().equals(value.toString());
    }
}
