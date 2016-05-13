package org.om.criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import org.jayson.parser.StaticFactory;
import org.om.element.Element;

/**
 *
 * @author tofar
 */
public class And implements Criteria {

    private final Criteria[] criteria;

    private And(Criteria[] criteria) {
        this.criteria = criteria;
    }

    @StaticFactory({"criteria"})
    public static Criteria valueOf(Criteria... criteria) {
        return valueOf(Arrays.asList(criteria));
    }

    public static Criteria valueOf(List<Criteria> criteria) {
        criteria = flatten(criteria);
        switch (criteria.size()) {
            case 0:
                return null;
            case 1:
                return criteria.get(0);
            default:
                return new And(criteria.toArray(new Criteria[criteria.size()]));
        }
    }

    private static List<Criteria> flatten(List<Criteria> criteria) {
        List<Criteria> ret = null;
        for (int i = 0; i < criteria.size(); i++) {
            Criteria c = criteria.get(i);
            if (c instanceof And) {
                if (ret == null) {
                    ret = new ArrayList<>();
                    for (int j = 0; j < i; j++) {
                        ret.add(criteria.get(j));
                    }
                }
                ret.addAll(Arrays.asList(((And) c).criteria));
            } else if (ret != null) {
                ret.add(c);
            }
        }
        return (ret == null) ? criteria : ret;
    }

    @Override
    public boolean match(Element element) {
        for(Criteria c : criteria){
            if(!c.match(element)){
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String getDescription(ResourceBundle resources) {
        StringBuilder str = new StringBuilder();
        str.append(resources.getString("CRITERIA_DESC_AND")).append(System.lineSeparator());
        for(Criteria c : criteria){
            str.append(Criteria.indent(c.getDescription(resources))).append(System.lineSeparator());
        }
        return str.toString();
    }

    public Criteria[] getCriteria() {
        return criteria.clone();
    }

    public int numCriteria() {
        return criteria.length;
    }

    public Criteria getCriteria(int index) {
        return criteria[index];
    }
}
