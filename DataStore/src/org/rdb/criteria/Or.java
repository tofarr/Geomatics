package org.rdb.criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jayson.element.Element;
import org.jayson.parser.StaticFactory;

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
    public static  Criteria valueOf(Criteria... criteria) {
        return valueOf(Arrays.asList(criteria));
    }

    public static  Criteria valueOf(List<Criteria> criteria) {
        List<Criteria> out = new ArrayList<>();
        for (Criteria c : criteria) {
            if (c == All.INSTANCE) {
                return c;
            } else if (c != None.INSTANCE) {
                flatten(c, out);
            }
        }
        switch (out.size()) {
            case 0:
                return None.INSTANCE;
            case 1:
                return out.get(0);
            default:
                return new Or(out.toArray(new Criteria[out.size()]));
        }
    }

    static  void flatten(Criteria criteria, List<Criteria> results) {
        if (criteria == null) {
            return;
        }
        if (!(criteria instanceof Or)) {
            results.add(criteria);
            return;
        }
        Or or = (Or) criteria;
        results.addAll(Arrays.asList(or.criteria));
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
