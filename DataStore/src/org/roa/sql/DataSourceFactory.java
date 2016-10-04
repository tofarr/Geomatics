package org.roa.sql;

import javax.sql.DataSource;

/**
 *
 * @author tofarr
 */
public interface DataSourceFactory {

    DataSource create();
}
