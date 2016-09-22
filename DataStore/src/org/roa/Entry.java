package org.roa;

import org.ds.element.model.ObjElement;

/**
 *
 * @author tofarr
 * @param <V>
 */
public interface Entry<V> extends Resource {
    
    long getId();

    V getValue();

    boolean exists();

    void setValue(V value);

    boolean remove();

    ObjElement toElement();
}
