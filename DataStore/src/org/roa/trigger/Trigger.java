
package org.roa.trigger;

import org.roa.Entry;
import org.roa.ResourceException;
import org.roa.SecurityContext;
import org.roa.Store;

/**
 *
 * @author tofarr
 */
public interface Trigger<E> {
    
    void exec(SecurityContext context, TriggerEvent event, Entry<E> entry, Store<E> store) throws ResourceException;
}
