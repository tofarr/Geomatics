package org.rdb;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.jayson.JaysonInput;
import org.jayson.element.Element;
import org.rdb.criteria.Criteria;
import org.rdb.security.Entitlements;
import org.rdb.security.SecurityContext;
import org.rdb.sorter.Sorter;

/**
 *
 * @author tofarr
 */
public interface Node {

    /**
     * Get the unique identifier for the address of this node
     *
     * @return
     */
    String getId();

    /**
     * Get the type for this node
     *
     * @return
     */
    NodeType getType();

    /**
     * Get the ETag for this node
     *
     * @return
     */
    String getETag();

    /**
     * Get the time index when this node was last modified - may reflect that
     * from parent.
     *
     * @return
     */
    long getLastModified();

    /**
     * Get the template for this node.
     *
     * @return
     */
    NodeTemplate getTemplate();

    /**
     * Get the entitlements for this node - these may have been inherited from a
     * parent node
     *
     * @return
     */
    Entitlements getEntitlements();
    
    
    Node find(SecurityContext context, NodePath path);

    long count(SecurityContext context, Criteria criteria);
    
    boolean query(SecurityContext context, Criteria criteria, Sorter sorter, NodeProcessor processor);
    
    List<Node> query(SecurityContext context, Criteria criteria, Sorter sorter, long offset, int length);
    
    /**
     * Read content - if type is data this will be raw data, otherwise it will
     * be json
     * @param context
     * @return 
     * @throws NodeException
     */
    InputStream read(SecurityContext context) throws NodeException;
    
    /**
     * Get element - if type is data this will be raw data, it will be
     * converted to string format
     * @param context
     * @return 
     * @throws NodeException
     */
    Element get(SecurityContext context) throws NodeException;
    
    /**
     * Read content - if type is data this will be raw data, otherwise it will
     * be json
     * @param context
     * @return 
     * @throws NodeException
     */
    OutputStream write(SecurityContext context) throws NodeException;
    
    /**
     * Read content - if type is data this will be raw data, otherwise it will be json
     * @param context
     * @param element
     */
    void update(SecurityContext context, Element element) throws NodeException;
    
    /**
     * Add child - if type is data this will be raw data, otherwise it will be json
     * @param context
     * @param id
     * @param element
     */
    void addChild(SecurityContext context, String id, Element element);
    
    /**
     * Add child - if type is data this will be raw data, otherwise it will be json
     * @param context
     * @param element
     * @return 
     */
    String addChild(SecurityContext context, Element element);

    boolean updateChild(SecurityContext context, String key, JaysonInput input);

    boolean removeChild(SecurityContext context, String key);

    void lock(SecurityContext context, boolean preventRead, long timeout);

    boolean unlock(SecurityContext context);
}
