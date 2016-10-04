
package org.resourcedb;

import org.jayson.element.Element;

/**
 * A workspace contains a tree, which we can get or update portions of as required.
 * 
 * Nodes differ from other items in that they place restrictions on what may be inserted into them (or supply default values when missing)
 * @author tofarr
 */
public interface Workspace {
    
    /**
     * Get a segment of the workspace as an element
     * @param context
     * @param path
     * @return 
     */
    Element get(SecurityContext context, Path path);
    
    
    voi
}
