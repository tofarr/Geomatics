package org.lcd.criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jayson.parser.StaticFactory;

/**
 *
 * @author tofar
 * @param <E>
 */
public class And<E> implements Criteria<E> {

    private final Criteria<E>[] criteria;

    private And(Criteria<E>[] criteria) {
        this.criteria = criteria;
    }

    @StaticFactory({"criteria"})
    public static <E> Criteria<E> valueOf(Criteria<E>... criteria) {
        return valueOf(Arrays.asList(criteria));
    }

    public static <E> Criteria<E> valueOf(List<Criteria<E>> criteria) {
        List<Criteria<E>> out = new ArrayList<>();
        for (Criteria<E> c : criteria) {
            if(c == None.INSTANCE){
                return c;
            }else if(c != All.INSTANCE){
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

    static <E> void flatten(Criteria<E> criteria, List<Criteria<E>> results) {
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
    public boolean match(Object value) {
        for (Criteria c : criteria) {
            if (!c.match(value)) {
                return false;
            }
        }
        return true;
    }

    public Criteria<E>[] getCriteria() {
        return criteria.clone();
    }

    public int numCriteria() {
        return criteria.length;
    }

    public Criteria getCriteria(int index) {
        return criteria[index];
    }
}
