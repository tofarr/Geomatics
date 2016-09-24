package org.roa.criteria.value;

import org.roa.criteria.Criteria;
import org.roa.element.Element;
import static org.roa.element.ElementType.BOOLEAN;
import static org.roa.element.ElementType.NUMBER;
import static org.roa.element.ElementType.STRING;
import org.roa.element.NumElement;
import org.roa.element.StrElement;

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
