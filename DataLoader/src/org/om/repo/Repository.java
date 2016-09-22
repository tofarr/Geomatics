package org.om.repo;

import org.om.store.Capabilities;

/**
 *
 * @author tofarr
 */
public interface Repository {

    Capabilities getCapabilities();
    
    ElementStore get(String name); 
    
}
