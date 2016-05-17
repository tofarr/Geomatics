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
        List<Criteria> out = new ArrayList<>();
        for (Criteria c : criteria) {
            if (c == None.INSTANCE) {
                return c;
            } else if (c != All.INSTANCE) {
                flatten(c, out);
            }
        }
        switch (out.size()) {
            case 0:
                return All.INSTANCE;
            case 1:
                return out.get(0);
            default:
                return new And(out.toArray(new Criteria[out.size()]));
        }
    }

    static <E> void flatten(Criteria criteria, List<Criteria> results) {
        if (criteria == null) {
            return;
        }
        if (!(criteria instanceof And)) {
            results.add(criteria);
            return;
        }
        And and = (And) criteria;
        results.addAll(Arrays.asList(and.criteria));
    }

    @Override
    public boolean match(Element element) {
        for (Criteria c : criteria) {
            if (!c.match(element)) {
                return false;
            }
        }
        return true;
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

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof And) && Arrays.equals(criteria, ((And) obj).criteria);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Arrays.hashCode(this.criteria);
        return hash;
    }

}
