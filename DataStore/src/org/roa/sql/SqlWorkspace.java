
package org.roa.sql;

import org.roa.Entitlements;
import org.roa.Processor;
import org.roa.ResourceException;
import org.roa.SecurityContext;
import org.roa.Store;
import org.roa.StoreInfo;
import org.roa.Workspace;
import org.rdb.trigger.Trigger;
import org.rdb.trigger.TriggerContext;
import org.rdb.trigger.TriggerEvent;

/**
 *
 * @author tofarr
 */
public class SqlWorkspace implements Workspace {
    
    
    

    @Override
    public Entitlements getEntitlements() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Store get(SecurityContext context, String name) throws ResourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean exists(SecurityContext context, String name) throws ResourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean remove(SecurityContext context, String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean query(SecurityContext context, Processor<Store> processor) throws ResourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void add(SecurityContext context, StoreInfo info) throws ResourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void update(SecurityContext context, StoreInfo info) throws ResourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TriggerContext addTrigger(SecurityContext context, Trigger trigger) throws ResourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeTrigger(long id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TriggerContext getTrigger(long id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean queryTriggers(SecurityContext context, Processor<Store> processor) throws ResourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean queryTriggers(SecurityContext context, TriggerEvent event, Processor<Store> processor) throws ResourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
