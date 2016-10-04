package org.rdb;

import java.beans.ConstructorProperties;
import org.jayson.element.Element;
import org.rdb.criteria.Criteria;
import org.roa.Entitlements;

/**
 *
 * @author tofarr
 */
public class NodeTemplate {

    /**
     * Type for this node
     */
    public final NodeType type;
    /**
     * Criteria which all nodes having this template must match. Typically, we
     * dont apply elements from sub templates here.
     */
    public final Criteria criteria;
    /**
     * Entitlements for nodes matching this template. (Only Object and Array
     * nodes will ever have entitlements relating to child nodes)
     */
    public final Entitlements templateEntitlements;
    /**
     * Entitlements for nodes matching this template. (Only Object and Array
     * nodes will ever have entitlements relating to child nodes)
     */
    public final Entitlements nodeEntitlements;
    /**
     * Cache control for nodes
     */
    public final CacheControl cacheControl;
    /**
     * Is this template virtual or explicit? Virtual templates typically include
     * no criteria or child templates and inherited cache settings and
     * entitlements
     */
    public final boolean isVirtual;
    /**
     * Default json - may be null
     */
    public final Element defaultValue;
    
    @ConstructorProperties({"type", "criteria", "templateEntitlements", "nodeEntitlements", "cacheControl", "isVirtual", "defaultValue"})
    public NodeTemplate(NodeType type, Criteria criteria, Entitlements templateEntitlements, Entitlements nodeEntitlements, CacheControl cacheControl, boolean isVirtual, Element defaultValue) {
        this.type = type;
        this.criteria = criteria;
        this.templateEntitlements = templateEntitlements;
        this.nodeEntitlements = nodeEntitlements;
        this.cacheControl = cacheControl;
        this.isVirtual = isVirtual;
        this.defaultValue = defaultValue;
    }
}
