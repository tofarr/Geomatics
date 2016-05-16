package org.lcd;

import org.lcd.filter.Filter;
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
    public boolean load(AttrSet attrs, Filter filter, SortOrder sortOrder, ResultIteratorProcessor processor) {
        return dataSource.load(attrs, filter, sortOrder, processor);
    }

    @Override
    public long count(Filter filter) {
        return dataSource.count(filter);
    }

}
