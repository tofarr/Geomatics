package org.lcd;

import java.util.List;
import org.lcd.criteria.Criteria;

/**
 *
 * @author tofar
 */
public abstract class WritableDataSource extends DataSource {

    public WritableDataSource(AttrSet attrs) throws NullPointerException {
        super(attrs);
    }

    public abstract long update(Criteria criteria, Result values);

    public abstract void create(Result result);

    public abstract void remove(Criteria criteria);
    
    public abstract void createAll(ResultIterator results);
    
    public abstract void createAll(List<Result> results);
    
}
