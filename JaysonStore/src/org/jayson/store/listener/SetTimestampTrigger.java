
package org.jayson.store.listener;

import java.beans.ConstructorProperties;
import org.jayson.element.Element;
import org.jayson.element.NumElement;
import org.jayson.store.Path;
import org.jayson.store.Store;
import org.jayson.store.security.AuthorizationToken;

/**
 *
 * @author tofarr
 */
public class SetTimestampTrigger extends EditListener {
    
    public final Path timestampPath;

    @ConstructorProperties({"priority","timestampPath"})
    public SetTimestampTrigger(int priority, Path timestampPath) {
        super(priority);
        this.timestampPath = timestampPath;
    }

    @Override
    public Element execute(AuthorizationToken context, Store store, Path triggerPath, Path elementPath, Element oldValue, Element newValue) throws TriggerException {
        Triggers take an update in and convert it into an update out...
        return timestampPath.updateElement(newValue, NumElement.valueOf(System.currentTimeMillis()));
        
    }
}
