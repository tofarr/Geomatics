package org.lcd.filter;

import org.jayson.parser.StaticFactory;
import org.lcd.Result;
import org.lcd.ResultIterator;
import org.lcd.criteria.All;
import org.lcd.criteria.Criteria;
import org.lcd.criteria.None;

/**
 *
 * @author tofar
 */
public class AttrFilter extends Filter {

    private final String attrName;
    private final Criteria criteria;

    private AttrFilter(String attrName, Criteria criteria) {
        this.attrName = attrName;
        this.criteria = criteria;
    }

    @StaticFactory({"attrName", "criteria"})
    public static Filter valueOf(String attrName, Criteria criteria) {
        if(attrName.isEmpty()){
            throw new IllegalArgumentException("attrName must not be empty");
        }
        if(criteria == All.INSTANCE){
            return AllFilter.INSTANCE;
        }else if(criteria == None.INSTANCE){
            return NoneFilter.INSTANCE;
        }else{
            return new AttrFilter(attrName, criteria);
        }
    }

    public String getAttrName() {
        return attrName;
    }

    public Criteria getCriteria() {
        return criteria;
    }

    @Override
    public boolean match(Result result) {
        Object attrValue = result.getByName(attrName);
        return criteria.match(attrValue);
    }

    @Override
    public boolean matchCurrent(ResultIterator iter) {
        Object attrValue = iter.getByName(attrName);
        return criteria.match(attrValue);
    }
}
