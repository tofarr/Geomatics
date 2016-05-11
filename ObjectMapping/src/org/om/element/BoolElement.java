package org.om.element;

import org.jayson.JaysonOutput;

/**
 *
 * @author tofar
 */
public final class BoolElement extends Element implements ValueElement<Boolean> {

    public static final BoolElement TRUE = new BoolElement(true);
    public static final BoolElement FALSE = new BoolElement(false);
    public static final StrElement TRUE_STR = new StrElement("true");
    public static final StrElement FALSE_STR = new StrElement("false");

    public final boolean bool;

    private BoolElement(boolean bool) {
        this.bool = bool;
    }

    public static BoolElement valueOf(boolean bool) {
        return bool ? TRUE : FALSE;
    }

    @Override
    public ElementType getType() {
        return ElementType.BOOLEAN;
    }

    @Override
    public StrElement asStr() {
        return bool ? TRUE_STR : FALSE_STR;
    }

    @Override
    public NumElement asNum() {
        return bool ? NumElement.ONE : NumElement.ZERO;
    }

    @Override
    public BoolElement asBool() {
        return this;
    }

    @Override
    public void toJson(JaysonOutput output) {
        output.bool(bool);
    }
    
    @Override
    public boolean matches(Element other) {
        if(other instanceof ValueElement){
            StrElement str = ((ValueElement)other).asStr();
            return asStr().value.equals(str.value);
        }
        return false;
    }

    @Override
    public Boolean getValue() {
        return bool;
    }
}
