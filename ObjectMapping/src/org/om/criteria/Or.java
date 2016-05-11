package org.om.criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jayson.parser.StaticFactory;
import org.om.element.Element;

/**
 *
 * @author tofar
 */
public class Or implements Criteria {

    private final Criteria[] criteria;

    private Or(Criteria[] criteria) {
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
                return new Or(criteria.toArray(new Criteria[criteria.size()]));
        }
    }

    private static List<Criteria> flatten(List<Criteria> criteria) {
        List<Criteria> ret = null;
        for (int i = 0; i < criteria.size(); i++) {
            Criteria c = criteria.get(i);
            if (c instanceof Or) {
                if (ret == null) {
                    ret = new ArrayList<>();
                    for (int j = 0; j < i; j++) {
                        ret.add(criteria.get(j));
                    }
                }
                ret.addAll(Arrays.asList(((Or) c).criteria));
            } else if (ret != null) {
                ret.add(c);
            }
        }
        return (ret == null) ? criteria : ret;
    }

    @Override
    public boolean match(Element element) {
        for (Criteria c : criteria) {
            if (c.match(element)) {
                return true;
            }
        }
        return false;
    }
}
