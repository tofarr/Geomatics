/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.om.criteria;

import org.om.element.Element;

/**
 *
 * @author tofar
 */
public interface Criteria {
    
    boolean match(Element element);
}
