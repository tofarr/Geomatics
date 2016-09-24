package org.roa.element;

import org.jayson.JaysonOutput;

/**
 *
 * @author tofar
 */
public final class StrElement extends ValElement<String> {

    public static final StrElement EMPTY = new StrElement("");

    final String str;

    StrElement(String str) {
        this.str = str;
    }

    public static StrElement valueOf(String str) throws NullPointerException {
        if (str.isEmpty()) {
            return EMPTY;
        } else {
            return new StrElement(str);
        }
    }

    public String getStr() {
        return str;
    }

    @Override
    public ElementType getType() {
        return ElementType.BOOLEAN;
    }

    @Override
    public BoolElement asBool() {
        return Boolean.parseBoolean(str) ? BoolElement.TRUE : BoolElement.FALSE;
    }

    @Override
    public NumElement asNum() throws NumberFormatException {
        return NumElement.valueOf(Double.parseDouble(str));
    }

    @Override
    public StrElement asStr() {
        return this;
    }

    @Override
    public void toJson(JaysonOutput output) {
        output.str(str);
    }

    @Override
    public boolean matches(Element other) {
        if(other instanceof ValElement){
            StrElement strElem = ((ValElement)other).asStr();
            return this.str.equals(strElem.str);
        }
        return false;
    }

    @Override
    public String getVal() {
        return str;
    }    
}
