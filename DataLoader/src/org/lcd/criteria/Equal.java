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
        if(value == null){
            return matchNull(this.value == null, true);
        }else if(this.value == null){
            return matchNull(true, false);
        }else if(this.value.getClass() == value.getClass()){
            return matchObj(this.value, (E)value);
        }else if(value instanceof Number){
            if(this.value instanceof Number){
                return matchNum(((Number)this.value).doubleValue(), ((Number)value).doubleValue());
            }else{
                try{
                    double v = Double.parseDouble(this.value.toString());
                    return matchNum(v, ((Number)value).doubleValue());
                }catch(NumberFormatException ex){
                    return matchStr(this.value.toString(), value.toString());
                }    
            }                
        }else if(this.value instanceof Number){
            try{
                double v = Double.parseDouble(value.toString());
                return matchNum(v, ((Number)this.value).doubleValue());
            }catch(NumberFormatException ex){
                return matchStr(this.value.toString(), value.toString());
            }
        }else{
            return matchStr(value.toString(), value.toString());
        }
    }
    
    protected boolean matchObj(E a, E b){
        return a.equals(b);
    }
    
    protected boolean matchNum(double a, double b){
        return a == b;
    }
    
    protected boolean matchStr(String a, String b){
        return a.equals(b);
    }
    
    protected boolean matchNull(boolean aNull, boolean bNull){
        return aNull == bNull;
    }
}
