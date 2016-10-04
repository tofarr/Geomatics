package org.jayson.store;

import java.beans.ConstructorProperties;
import org.jayson.element.Element;

/**
 * Wildcards in paths imply that a key should be generated.
 * Null element implies that an element should be deleted.
 * @author tofarr
 */
public class Edit {
    
    public final Path path;
    public final Element element;
    /** Flag indicating that this should be merged with existing element (rather than replacing it) */
    public boolean merge;
    
    @ConstructorProperties({"path", "element", "merge"})
    public Edit(Path path, Element element, boolean merge){
        this.path = path;
        this.element = element;
        this.merge = merge;
    }
}
