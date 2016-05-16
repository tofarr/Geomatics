package org.lcd;

import java.util.List;
import org.lcd.filter.Filter;

/**
 *
 * @author tofar
 */
public abstract class WritableDataSource extends DataSource {

    public WritableDataSource(AttrSet attrs) throws NullPointerException {
        super(attrs);
    }

    public abstract long update(Filter filter, Result values);

    public abstract void create(Result result);

    public abstract long remove(Filter filter);
    
    public abstract long createAll(ResultIterator results);
    
    public abstract void createAll(List<Result> results);
    
}
