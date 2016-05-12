package org.om.element;

import org.jayson.JaysonOutput;

/**
 *
 * @author tofar
 */
public final class NumElement extends ValueElement<Double> {
    
    public static final NumElement ZERO = new NumElement(0);
    public static final NumElement ONE = new NumElement(1);
    
    public final double number;

    private NumElement(double number) {
        this.number = number;
    }
    
    public static NumElement valueOf(double number){
        if(number == 0){
            return ZERO;
        }else if(number == 1){
            return ONE;
        }else{
            return new NumElement(number);
        }
    }

    @Override
    public ElementType getType() {
        return ElementType.NUMBER;
    }
    
    @Override
    public BoolElement asBool(){
        return (number != 0) ? BoolElement.TRUE : BoolElement.FALSE;
    }

    @Override
    public StrElement asStr() {
        return new StrElement(Double.toString(number));
    }

    @Override
    public NumElement asNum() throws NumberFormatException {
        return this;
    }

    @Override
    public void toJson(JaysonOutput output) {
        output.num(number);
    }

    @Override
    public boolean matches(Element other) {
        if(other instanceof ValueElement){
            StrElement str = ((ValueElement)other).asStr();
            return Double.toString(number).equals(str.value);
        }
        return false;
    }

    @Override
    public Double getValue() {
        return number;
    }
}
