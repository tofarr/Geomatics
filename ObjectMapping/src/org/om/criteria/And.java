package org.om.criteria;

import org.om.schema.Result;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jayson.parser.StaticFactory;
import org.om.element.Element;
import org.om.schema.Path;

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
    public Result validate(Path path, Element element) {
        Result[] children = new Result[criteria.length];
        boolean success = true;
        for(int c = criteria.length; c-- > 0;){
            Result result = criteria[c].validate(path, element);
            success &= result.isSuccess();
            children[c] = result;
        }
        
        return new Result(path, success, null, null, children);
    }
}
