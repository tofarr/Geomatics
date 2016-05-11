package org.om.element;

/**
 *
 * @author tofar
 */
public interface ValueElement<C extends Comparable> {

    BoolElement asBool();

    StrElement asStr();

    NumElement asNum() throws NumberFormatException;
    
    C getValue();
}
