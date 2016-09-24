package org.roa;

import org.roa.trigger.Trigger;
import org.roa.trigger.TriggerContext;
import org.roa.trigger.TriggerEvent;

/**
 *
 * @author tofarr
 */
public interface Workspace {

    /**
     * Get entitlements on this store - 
     * @return 
     */
    Entitlements getEntitlements();
    
    Store get(SecurityContext context, String name) throws ResourceException;
    
    boolean exists(SecurityContext context, String name) throws ResourceException;
    
    boolean remove(SecurityContext context, String name);
    
    boolean query(SecurityContext context, Processor<Store> processor) throws ResourceException;
    
    void add(SecurityContext context, StoreInfo info) throws ResourceException;
    
    void update(SecurityContext context, StoreInfo info) throws ResourceException;
    
    TriggerContext addTrigger(SecurityContext context, Trigger trigger) throws ResourceException;
    
    void removeTrigger(long id);
    
    TriggerContext getTrigger(long id);
    
    boolean queryTriggers(SecurityContext context, Processor<Store> processor) throws ResourceException;
    
    boolean queryTriggers(SecurityContext context, TriggerEvent event, Processor<Store> processor) throws ResourceException;
    
}
