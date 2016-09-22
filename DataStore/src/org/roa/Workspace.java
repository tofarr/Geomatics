package org.roa;

/**
 *
 * @author tofarr
 */
public interface Workspace extends Resource {

    void add(StoreSpec spec) throws ResourceException;
    
    Store get(String name) throws ResourceException;
    
    boolean exists(String name) throws ResourceException;
    
    void update(StoreSpec spec) throws ResourceException;
    
    boolean remove(String name);
    
    boolean iterate(Processor<Store> processor) throws ResourceException;
    
    // triggers can be applied to both repositories and stores.
    
    // this is a difference between DDL and DML, but we merge both
    
    // when adding a trigger, it gets added to the current root repository
    
    //you get a factory which yields a repository
    
    //trigger stores reference to application - so we add top level app / repo when triggering
    
    //need to spec out security before triggers
    
    // ok - we have application - it has no constraints
    
    //factory
    //
    
    
    //Factory - getApplication(credentials);
    //Application - getRepository(name)
    //Repository - getStore(name)
    //Store - entries
    //security and triggers must work together - but how?
    
    //trigger - maybe global and integrated?
    //separate actions and triggers.
    
    //workspace
    
    //store: system/actions

    //action - added to action space
    
    //onBeforeCreate
    
    
    
    
    //Entry
    //Store
    //Repository
    //Application
    
    
    //store entitlements by user
    
    //users/entitlements
    
    //log in, get entitlements
    
    
    
    //security
    
    //each resource has entitlements - add,get,update,remove,iterate 
   
    //may have a collection of groups, and which entitlements they posess
    
    //every action has a context passed to it. This has the current set of groups
    
    //triggers hold on to this context - it is used when they are invoked.
}
