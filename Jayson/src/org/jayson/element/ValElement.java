package org.jayson.element;

/**
 *
 * @author tofar
 */
public abstract class ValElement<C extends Comparable> extends Element {

    ValElement() {
    }

    public abstract BoolElement asBool();

    public abstract StrElement asStr();

    public abstract NumElement asNum() throws NumberFormatException;
    
    public abstract C getVal();
}
