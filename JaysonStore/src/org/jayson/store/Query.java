
package org.jayson.store;

import java.beans.ConstructorProperties;
import org.jayson.store.criteria.All;
import org.jayson.store.criteria.Criteria;

/**
 *
 * @author tofarr
 */
public class Query {
    /** Wildcard in path implies return all */
    public final Path path;
    /** Is sub path of path - Empty implies just return current */
    public final Path retrievePath;
    /** Optional criteria to apply to result. */
    public final Criteria criteria;
    
    @ConstructorProperties({"path", "retrievePath", "criteria"})
    public Query(Path path, Path retrievePath, Criteria criteria) throws NullPointerException {
        if(path == null){
            throw new NullPointerException("path must not be null");
        }
        this.path = path;
        this.retrievePath = (retrievePath == null) ? Path.EMPTY : retrievePath;
        this.criteria = (criteria == null) ? All.INSTANCE : criteria;
        
    }
}
