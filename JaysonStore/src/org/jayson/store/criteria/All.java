
package org.jayson.store.criteria;

import org.jayson.element.Element;

/**
 *
 * @author tofarr
 */
public class All implements Criteria {
    
    public static final All INSTANCE = new All();

    @Override
    public boolean match(Element element) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void check(Element element) throws CriteriaException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
