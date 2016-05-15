package org.lcd.criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jayson.parser.StaticFactory;
import org.lcd.Result;
import org.lcd.ResultIterator;

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
            flatten(c, out);
        }
        switch (out.size()) {
            case 0:
                return null;
            case 1:
                return out.get(0);
            default:
                return new And(out.toArray(new Criteria[out.size()]));
        }
    }

    static void flatten(Criteria criteria, List<Criteria> results) {
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
    public boolean match(Result result) {
        for (Criteria c : criteria) {
            if (!c.match(result)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean matchCurrent(ResultIterator iter) {
        for (Criteria c : criteria) {
            if (c.matchCurrent(iter)) {
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
}
