package org.lcd.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lcd.Result;
import org.lcd.ResultIterator;
import org.lcd.criteria.All;
import org.lcd.criteria.Criteria;
import org.lcd.criteria.Or;

/**
 *
 * @author tofar
 */
public class OrFilter extends Filter {

    private final Filter[] filters;

    private OrFilter(Filter[] filters) {
        this.filters = filters;
    }

    public static Filter valueOf(Filter... filters) {
        return valueOf(Arrays.asList(filters));
    }

    public static Filter valueOf(List<Filter> filters) {
        //any and filters in the list have their content flattened
        List<Filter> flattened = new ArrayList<>(filters.size());
        Map<String, Integer> attrIndices = new HashMap<>();
        for (Filter filter : filters) {
            Filter ret = add(filter, flattened, attrIndices);
            if (ret != null) {
                return ret;
            }
        }
        switch (filters.size()) {
            case 0:
                return NoneFilter.INSTANCE;
            case 1:
                return filters.get(0);
            default:
                return new OrFilter(filters.toArray(new Filter[filters.size()]));
        }
    }

    static Filter add(Filter filter, List<Filter> flattened, Map<String, Integer> attrIndices) {
        if (filter instanceof OrFilter) {
            OrFilter or = (OrFilter) filter;
            for (Filter f : or.filters) {
                Filter ret = add(f, flattened, attrIndices);
                if (ret != null) {
                    return ret;
                }
            }
            return null;
        } else if (filter == AllFilter.INSTANCE) {
            return AllFilter.INSTANCE;
        } else if (filter != NoneFilter.INSTANCE) {
            AttrFilter f = (AttrFilter) filter;
            Integer index = attrIndices.get(f.getAttrName());
            if (index == null) {
                attrIndices.put(f.getAttrName(), flattened.size());
                flattened.add(f);
            } else {
                AttrFilter old = (AttrFilter) flattened.get(index);
                Criteria criteria = Or.valueOf(old.getCriteria(), f.getCriteria());
                if (criteria == All.INSTANCE) {
                    return AllFilter.INSTANCE;
                }
                flattened.set(index, AttrFilter.valueOf(old.getAttrName(), criteria));
            }
        }
        return null;
    }

    public Filter[] getFilters() {
        return filters.clone();
    }

    @Override
    public boolean match(Result result) {
        for (Filter f : filters) {
            if (!f.match(result)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean matchCurrent(ResultIterator iter) {
        for (Filter f : filters) {
            if (!f.matchCurrent(iter)) {
                return false;
            }
        }
        return true;
    }

}
