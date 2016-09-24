package org.roa.criteria.value;

import java.util.Objects;
import org.roa.criteria.Criteria;
import org.roa.element.Element;
import org.roa.element.ValElement;

/**
 *
 * @author tofarrell
 */
public class StrContains implements Criteria {

    private final String value;

    public StrContains(String value) throws NullPointerException, IllegalArgumentException {
        if(value.isEmpty()){
            throw new IllegalArgumentException("value must not be empty");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    
    @Override
    public boolean match(Element element) {
        if(element instanceof ValElement){
            return ((ValElement)element).asStr().getStr().contains(value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StrContains other = (StrContains) obj;
        return Objects.equals(this.value, other.value);
    }
    
    
    
    

}
