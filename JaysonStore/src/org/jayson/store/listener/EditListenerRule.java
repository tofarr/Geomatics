package org.jayson.store.listener;

import java.beans.ConstructorProperties;
import org.jayson.store.AbstractRule;
import org.jayson.store.Path;

/**
 *
 * @author tofarr
 */
public class EditListenerRule extends AbstractRule {
    

    public final EditListener listener;

    @ConstructorProperties({"id", "priority", "path", "listener"})
    public EditListenerRule(long id, int priority, Path path, EditListener listener) {
        super(id, priority, path);
        this.listener = listener;
    }
}
