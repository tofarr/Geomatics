package org.lcd;

import java.util.ArrayList;
import java.util.List;
import org.lcd.criteria.Criteria;
import org.lcd.filter.Filter;
import org.lcd.sort.SortOrder;

/**
 *
 * @author tofar
 */
public class ArrayListDataSource extends WritableDataSource {

    private volatile ArrayList<Result> values;

    public ArrayListDataSource(AttrSet attrs, ArrayList<Result> values) throws NullPointerException {
        super(attrs);
        this.values = values;
    }

    public ArrayListDataSource(AttrSet attrs) throws NullPointerException {
        this(attrs, new ArrayList<>());
    }

    @Override
    public boolean load(AttrSet attrs, Filter filter, SortOrder sortOrder, ResultProcessor processor) {
        ArrayList<Result> ret = getResults(attrs, filter, sortOrder);
        for (Result result : ret) {
            if (!processor.process(result)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean load(final AttrSet attrs, Filter filter, SortOrder sortOrder, ResultIteratorProcessor processor) {
        final ArrayList<Result> results = getResults(attrs, filter, sortOrder);
        return processor.process(new ResultIterator() {
            int index = -1;

            @Override
            public boolean next() {
                return ++index < results.size();
            }

            @Override
            public AttrSet getAttrs() {
                return attrs;
            }

            @Override
            public Object getByName(String name) {
                return results.get(index).getByName(name);
            }

            @Override
            public Object getByIndex(int index) {
                return results.get(index).getByIndex(index);
            }

            @Override
            public long skip(long amt) {
                long ret = Math.min(((long) results.size()) - index, amt);
                index += (int) ret;
                return ret;
            }

        });
    }

    private ArrayList<Result> getResults(AttrSet attrs, Filter filter, SortOrder sortOrder) {
        ArrayList<Result> allValues = values;
        ArrayList<Result> ret = new ArrayList<>();
        for (Result result : allValues) {
            if ((filter == null) || filter.match(result)) {
                if (attrs == null) {
                    ret.add(result);
                } else {
                    ret.add(result.filter(attrs));
                }
            }
        }
        if (sortOrder != null) {
            ret.sort(sortOrder);
        }
        return ret;
    }

    @Override
    public long count(Filter filter) {
        ArrayList<Result> allValues = values;
        if (filter == null) {
            return allValues.size();
        } else {
            int ret = 0;
            for (Result result : allValues) {
                if (filter.match(result)) {
                    ret++;
                }
            }
            return ret;
        }
    }

    @Override
    public synchronized long update(Filter filter, Result newValue) {
        ArrayList<Result> allValues = values;
        ArrayList<Result> newValues = new ArrayList<>(allValues.size());
        int count = 0;
        for (Result result : allValues) {
            if (filter.match(result)) {
                newValues.add(result.copyFrom(newValue));
                count++;
            }else{
                newValues.add(result);
            }
        }
        values = newValues;
        return count;
    }

    @Override
    public synchronized void create(Result result) {
        result = result.filter(attrs);
        values.add(result);
    }

    @Override
    public synchronized long remove(Filter filter) {
        ArrayList<Result> allValues = values;
        ArrayList<Result> newValues = new ArrayList<>(allValues.size());
        int ret = 0;
        for(Result value : allValues){
            if(!filter.match(value)){
                newValues.add(value);
            }else{
                ret++;
            }
        }
        values = newValues;
        return ret;
    }

    @Override
    public synchronized long createAll(ResultIterator results) {
        ArrayList<Result> newValues = new ArrayList<>(values);
        int ret = 0;
        while(results.next()){
            newValues.add(new Result(results));
            ret++;
        }
        values = newValues;
        return ret;
    }

    @Override
    public synchronized void createAll(List<Result> results) {
        ArrayList<Result> newValues = new ArrayList<>(values.size() + results.size());
        newValues.addAll(values);
        newValues.addAll(results);
        values = newValues;
    }

}
