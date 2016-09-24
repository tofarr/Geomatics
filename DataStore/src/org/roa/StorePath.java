
package org.roa;

import java.beans.ConstructorProperties;

/**
 *
 * @author tofarr
 */
public class StorePath {
    
    public final String group;
    public final String store;

    @ConstructorProperties({"group","store"})
    public StorePath(String group, String store) {
        this.group = group;
        this.store = store;
    }
    
    public boolean exists(SecurityContext context, Application factory){
        Workspace storeGroup = factory.get(group);
        if(storeGroup != null){
            return storeGroup.get(context, store) != null;
        }
        return false;
    }
    
    public Store get(SecurityContext context, Application factory) throws ResourceException{
        Workspace storeGroup = factory.get(group);
        if(storeGroup != null){
            return storeGroup.get(context, store);
        }
        return null;
    }
}
