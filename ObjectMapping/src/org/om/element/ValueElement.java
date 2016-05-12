package org.om.element;

/**
 *
 * @author tofar
 */
public abstract class ValueElement<C extends Comparable> extends Element {

    ValueElement() {
    }

    public abstract BoolElement asBool();

    public abstract StrElement asStr();

    public abstract NumElement asNum() throws NumberFormatException;
    
    public abstract C getValue();
}
