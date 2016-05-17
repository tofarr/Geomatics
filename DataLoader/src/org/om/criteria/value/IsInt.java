package org.om.criteria.value;

import java.util.ResourceBundle;
import org.om.criteria.Criteria;
import org.om.element.Element;
import org.om.element.NumElement;
import org.om.element.StrElement;

/**
 *
 * @author tofar
 */
public final class IsInt implements Criteria {

    private static final IsInt INSTANCE = new IsInt();
    
    private IsInt(){
    }
    
    public static IsInt getInstance(){
        return INSTANCE;
    }
    
    @Override
    public boolean match(Element element) {
        switch(element.getType()){
            case BOOLEAN:
                return true;
            case NUMBER:
                double n = ((NumElement)element).getNum();
                return !(Double.isInfinite(n) || Double.isNaN(n) || (Math.floor(n) != n));
            case STRING:
                try{
                    Long.parseLong(((StrElement)element).getStr());
                    return true;
                }catch(NumberFormatException ex){
                    return false;
                }
            default:
                return false;
        }
    }
}
