package org.ds.element.criteria.value;

import org.roa.criteria.Criteria;
import org.ds.element.model.Element;
import static org.ds.element.model.ElementType.BOOLEAN;
import static org.ds.element.model.ElementType.NUMBER;
import static org.ds.element.model.ElementType.STRING;
import org.ds.element.model.NumElement;
import org.ds.element.model.StrElement;

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
