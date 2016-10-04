
package org.jayson.store.security;

import java.beans.ConstructorProperties;
import org.jayson.store.AbstractRule;
import org.jayson.store.Path;

/**
 *
 * @author tofarr
 */
public class EntitlementRule extends AbstractRule {
    
    public final Entitlements entitlements;
    public final boolean merge;

    @ConstructorProperties({"id", "priority", "path", "entitlements", "merge"})
    public EntitlementRule(long id, int priority, Path path, Entitlements entitlements, boolean merge) {
        super(id, priority, path);
        this.entitlements = entitlements;
        this.merge = merge;
    }
    
}
