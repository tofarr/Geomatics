package org.om.store.key;

import org.om.element.ElementType;
import org.om.element.ValElement;

/**
 *
 * @author tofar
 */
public interface KeyGenerator {

    ElementType getType();

    public ValElement createKey();

}
