package org.roa;

import org.roa.element.ObjElement;

/**
 *
 * @author tofarr
 */
public interface Entry<E> {

    long getId();

    long getLastModified();

    String getETag();

    /**
     * Get set of entitlements for this resource - governing what can be done
     * with it. Depending on the store this may be from element data or just
     * from the store itself
     *
     * @return
     */
    Entitlements getEntitlements();
    
    Store<E> getStore();

    void lock(SecurityContext context, LockType lockType, long timeout);

    void unlock(SecurityContext context);

    boolean exists();
    
    E getValue();

    void setValue(SecurityContext context, E value);

    boolean remove(SecurityContext context);

    ObjElement toElement();
}
