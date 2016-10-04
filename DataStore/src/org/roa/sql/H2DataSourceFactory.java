package org.roa.sql;

import java.beans.ConstructorProperties;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.roa.ResourceException;

/**
 *
 * @author tofarr
 */
public class H2DataSourceFactory implements DataSourceFactory{

    public final String url;
    public final String user;
    public final String password;

    @ConstructorProperties({"url", "user", "password"})
    public H2DataSourceFactory(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public DataSource create() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(url);
        dataSource.setUser(user);
        dataSource.setPassword(password);
        return dataSource;
    }

}
