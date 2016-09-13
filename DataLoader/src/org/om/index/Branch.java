package org.om.index;

import org.om.criteria.Criteria;

/**
 *
 * @author tofarr
 */
public class Branch implements Node {
    
    private final Criteria criteria;
    private final Node pass;
    private final Node fail;

}