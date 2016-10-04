package org.jayson.store.security;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.jayson.parser.StaticFactory;

/**
 *
 * @author tofarr
 */
public class Entitlements {

    public static final Entitlements EMPTY = new Entitlements(Collections.EMPTY_MAP);
    public static final int ADD_SPECIFY_ID = 1;
    public static final int ADD_GENERATE_ID = 2;
    public static final int READ = 4;
    public static final int UPDATE = 8;
    public static final int REMOVE = 16;
    public static final int LOCK = 32;
    public static final int ENTITLEMENTS = 64; // GET_ENTITLEMENTS is covered by read.
    public static final int LISTENERS = 128;
    public static final int VALIDATION = 256;
    public static final int DEFAULTS = 512;

    public static final int ALL = ADD_SPECIFY_ID 
        | ADD_GENERATE_ID 
        | READ 
        | UPDATE 
        | REMOVE 
        | LOCK 
        | ENTITLEMENTS 
        | LISTENERS 
        | VALIDATION
        | DEFAULTS;

    private final Map<String, Integer> permissions;

    private Entitlements(Map<String, Integer> permissions) {
        this.permissions = permissions;
    }

    @StaticFactory({"permissions"})
    public static Entitlements valueOf(Map<String, Integer> permissions) {
        if (permissions.isEmpty()) {
            return EMPTY;
        }
        HashMap<String, Integer> map = new HashMap<>();
        for (Map.Entry<String, Integer> entry : permissions.entrySet()) {
            int permission = entry.getValue() & ALL;
            if (permission != 0) {
                map.put(entry.getKey(), permission);
            }
        }
        return map.isEmpty() ? EMPTY : new Entitlements(Collections.unmodifiableMap(map));
    }

    public Map<String, Integer> getPermissions() {
        return permissions;
    }

    public boolean check(AuthorizationToken context, int requiredPermission) {
        for (String groupId : context.getGroupIds()) {
            if (check(groupId, requiredPermission)) {
                return true;
            }
        }
        return false;
    }

    public boolean check(String groupId, int requiredPermission) {
        Integer permission = permissions.get(groupId);
        return (permission != null) && ((permission & requiredPermission) == requiredPermission);
    }

    public Entitlements union(Entitlements other) {
        if (other.equals(this)) {
            return this;
        }
        Map<String, Integer> map = new HashMap<>(permissions);
        boolean allInThis = true;
        boolean allInOther = true;
        for (Map.Entry<String, Integer> entry : other.permissions.entrySet()) {
            String groupId = entry.getKey();
            Integer fromThis = map.get(groupId);
            Integer fromOther = entry.getValue();
            if (fromThis == null) {
                map.put(groupId, fromOther);
                allInThis = false;
            } else {
                int combo = fromThis | fromOther;
                allInThis &= (combo & fromThis) == fromThis;
                allInOther &= (combo & fromOther) == fromOther;
                map.put(groupId, combo);
            }
        }
        if (allInThis) {
            return this;
        } else if (allInOther) {
            return other;
        } else {
            return new Entitlements(Collections.unmodifiableMap(map));
        }
    }

    public Entitlements intersection(Entitlements other) {
        if (other.equals(this)) {
            return this;
        }
        Map<String, Integer> map = new HashMap<>();
        boolean allInThis = true;
        boolean allInOther = true;
        for (Map.Entry<String, Integer> entry : other.permissions.entrySet()) {
            String groupId = entry.getKey();
            Integer fromThis = map.get(groupId);
            if (fromThis == null) {
                allInOther = false;
            } else {
                Integer fromOther = entry.getValue();
                int combo = fromThis & fromOther;
                allInThis &= combo == fromThis;
                allInOther &= combo == fromOther;
                map.put(groupId, combo);
            }
        }
        if (allInThis) {
            return this;
        } else if (allInOther) {
            return other;
        } else {
            return new Entitlements(Collections.unmodifiableMap(map));
        }
    }

    @Override
    public int hashCode() {
        return this.permissions.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Entitlements) && ((Entitlements) obj).permissions.equals(permissions);
    }

}
