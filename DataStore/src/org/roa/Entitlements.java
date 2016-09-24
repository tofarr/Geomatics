package org.roa;

import java.beans.ConstructorProperties;

/**
 *
 * @author tofarr
 */
public class Entitlements {

    public static final int ADD = 1;
    public static final int GET = 2;
    public static final int UPDATE = 4;
    public static final int REMOVE = 8;
    public static final int QUERY = 16;
    public static final int LOCK = 32;
    
    private final long[] groups;
    private final int[] entitlements;
    
    @ConstructorProperties({"groups", "entitlements"})
    public Entitlements(long[] groups, int[] entitlements){
        this.groups = groups.clone();
        this.entitlements = entitlements.clone();
    }
    
    boolean canAdd(SecurityContext context) {
        return can(context, ADD);
    }

    boolean canGet(SecurityContext context) {
        return can(context, GET);
    }

    boolean canUpdate(SecurityContext context) {
        return can(context, UPDATE);
    }

    boolean canRemove(SecurityContext context) {
        return can(context, REMOVE);
    }

    boolean canQuery(SecurityContext context) {
        return can(context, QUERY);
    }
    
    boolean canLock(SecurityContext context) {
        return can(context, LOCK);
    }
    
    boolean can(SecurityContext context, int entitlement){
        for(int i = 0; i < entitlements.length; i++){
            if(((entitlements[i] & entitlement) == entitlement)
                && context.has(groups[i])){
                return true;
            }
        }
        return false;
    }

}
