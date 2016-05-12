package org.om.element;

import org.jayson.JaysonOutput;

/**
 *
 * @author tofar
 */
public final class StrElement extends ValueElement<String> {

    public static final StrElement EMPTY = new StrElement("");

    public final String value;

    StrElement(String value) {
        this.value = value;
    }

    public static StrElement valueOf(String value) throws NullPointerException {
        if (value.isEmpty()) {
            return EMPTY;
        } else {
            return new StrElement(value);
        }
    }

    @Override
    public ElementType getType() {
        return ElementType.BOOLEAN;
    }

    @Override
    public BoolElement asBool() {
        return Boolean.parseBoolean(value) ? BoolElement.TRUE : BoolElement.FALSE;
    }

    @Override
    public NumElement asNum() throws NumberFormatException {
        return NumElement.valueOf(Double.parseDouble(value));
    }

    @Override
    public StrElement asStr() {
        return this;
    }

    @Override
    public void toJson(JaysonOutput output) {
        output.str(value);
    }

    @Override
    public boolean matches(Element other) {
        if(other instanceof ValueElement){
            StrElement str = ((ValueElement)other).asStr();
            return value.equals(str.value);
        }
        return false;
    }

    @Override
    public String getValue() {
        return value;
    }    
}
