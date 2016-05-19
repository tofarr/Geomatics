
package org.om.store;

import java.beans.ConstructorProperties;

/**
 *
 * @author tofar
 */
public class Capabilities {
    
    public static final int CREATE = 1;
    public static final int GET = 2;
    public static final int UPDATE = 4;
    public static final int REMOVE = 8;
    public static final int QUERY = 16;
    public static final int LOCK_STORE = 32;
    public static final int LOCK_RECORD = 64;
    
    private final int capabilities;

    @ConstructorProperties({"capabilities"})
    public Capabilities(int capabilities) {
        this.capabilities = capabilities;
    }

    public int getCapabilities() {
        return capabilities;
    }
    
    public boolean canCreate(){
        return (capabilities & CREATE) != 0;
    }
    
    public boolean canGet(){
        return (capabilities & GET) != 0;
    }
    
    public boolean canUpdate(){
        return (capabilities & UPDATE) != 0;
    }

    public boolean canRemove(){
        return (capabilities & REMOVE) != 0;
    }
    
    public boolean canQuery(){
        return (capabilities & QUERY) != 0;
    }
    
    public boolean canLockStore(){
        return (capabilities & LOCK_STORE) != 0;
    }
    
    public boolean canLockRecord(){
        return (capabilities & LOCK_RECORD) != 0;
    }
}
