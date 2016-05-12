package org.om.element;

import org.jayson.JaysonOutput;

/**
 *
 * @author tofar
 */
public final class NumElement extends ValElement<Double> {
    
    public static final NumElement ZERO = new NumElement(0);
    public static final NumElement ONE = new NumElement(1);
    
    final double num;

    private NumElement(double num) {
        this.num = num;
    }
    
    public static NumElement valueOf(double num){
        if(num == 0){
            return ZERO;
        }else if(num == 1){
            return ONE;
        }else{
            return new NumElement(num);
        }
    }

    @Override
    public ElementType getType() {
        return ElementType.NUMBER;
    }

    public double getNum() {
        return num;
    }
    
    
    @Override
    public BoolElement asBool(){
        return (num != 0) ? BoolElement.TRUE : BoolElement.FALSE;
    }

    @Override
    public StrElement asStr() {
        return new StrElement(Double.toString(num));
    }

    @Override
    public NumElement asNum() throws NumberFormatException {
        return this;
    }

    @Override
    public void toJson(JaysonOutput output) {
        output.num(num);
    }

    @Override
    public boolean matches(Element other) {
        if(other instanceof ValElement){
            StrElement str = ((ValElement)other).asStr();
            return Double.toString(num).equals(str.str);
        }
        return false;
    }

    @Override
    public Double getVal() {
        return num;
    }
}
