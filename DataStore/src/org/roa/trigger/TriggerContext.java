
package org.roa.trigger;

import java.beans.ConstructorProperties;
import org.roa.Entry;
import org.roa.ResourceException;
import org.roa.SecurityContext;
import org.roa.Store;

/**
 *
 * @author tofarr
 * @param <E>
 */
public final class TriggerContext<E> {
    
    public final long id;
    public final Trigger<E> trigger;
    public final SecurityContext securityContext;

    @ConstructorProperties({"id", "trigger", "securityContext"})
    public TriggerContext(long id, Trigger trigger, SecurityContext securityContext) {
        this.id = id;
        this.trigger = trigger;
        this.securityContext = securityContext;
    }
    
    public void exec(TriggerEvent event, Entry<E> entry, Store<E> store) throws ResourceException {
        trigger.exec(securityContext, event, entry, store);
    }
}
