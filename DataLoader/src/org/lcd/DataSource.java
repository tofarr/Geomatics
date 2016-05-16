package org.lcd;

import org.lcd.criteria.Criteria;
import org.lcd.filter.Filter;
import org.lcd.sort.SortOrder;

/**
 *
 * @author tofar
 */
public abstract class DataSource {

    protected final AttrSet attrs;

    public DataSource(AttrSet attrs) throws NullPointerException {
        if (attrs == null) {
            throw new NullPointerException("attrs must not be null");
        }
        this.attrs = attrs;
    }

    public AttrSet getAttrs() {
        return attrs;
    }

    public boolean load(AttrSet attrs, Filter filter, SortOrder sortOrder, ResultProcessor processor) {
        return load(attrs, filter, sortOrder, new ResultIteratorProcessor() {
            @Override
            public boolean process(ResultIterator iterator) {
                while (iterator.next()) {
                    Result result = new Result(iterator);
                    if (!processor.process(result)) {
                        return false;
                    }
                }
                return true;
            }
        });
    }

    public abstract boolean load(AttrSet attrs, Filter filter, SortOrder sortOrder, ResultIteratorProcessor processor);

    public abstract long count(Filter filter);
}
