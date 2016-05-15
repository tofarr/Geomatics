package org.lcd;

import org.lcd.criteria.Criteria;
import org.lcd.sort.SortOrder;

/**
 *
 * @author tofar
 */
public class ReadOnlyDataSource extends DataSource {

    private final WritableDataSource dataSource;

    public ReadOnlyDataSource(WritableDataSource dataSource) throws NullPointerException {
        super(dataSource.getAttrs());
        this.dataSource = dataSource;
    }

    @Override
    public boolean load(AttrSet attrs, Criteria criteria, SortOrder sortOrder, ResultIteratorProcessor processor) {
        return dataSource.load(attrs, criteria, sortOrder, processor);
    }

    @Override
    public long count(Criteria criteria) {
        return dataSource.count(criteria);
    }

}
