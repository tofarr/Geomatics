package org.lcd.criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jayson.parser.StaticFactory;

/**
 *
 * @author tofar
 */
public class Or<E> implements Criteria<E> {

    private final Criteria<E>[] criteria;

    private Or(Criteria<E>[] criteria) {
        this.criteria = criteria;
    }

    @StaticFactory({"criteria"})
    public static <E> Criteria<E> valueOf(Criteria<E>... criteria) {
        return valueOf(Arrays.asList(criteria));
    }

    public static <E> Criteria<E> valueOf(List<Criteria<E>> criteria) {
        List<Criteria<E>> out = new ArrayList<>();
        for (Criteria<E> c : criteria) {
            if(c == All.INSTANCE){
                return c;
            }else if(c != None.INSTANCE){
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

    static <E> void flatten(Criteria<E> criteria, List<Criteria<E>> results) {
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
    public boolean match(E result) {
        for (Criteria<E> c : criteria) {
            if (c.match(result)) {
                return true;
            }
        }
        return false;
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
