package org.om.store.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.om.criteria.Criteria;
import org.om.sort.Sorter;
import org.om.store.ElementStore;
import org.om.store.StoreException;

/**
 *
 * @author tofar
 */
public class JdbcElementStore implements ElementStore {

    private final DataSource dataSource;

    public JdbcElementStore(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public boolean load(Criteria criteria, Sorter sorter, ElementProcessor processor) throws StoreException {
        try(Connection con = dataSource.getConnection()){
            NEED ONE WITH OBJECTS...
        }
    }

    @Override
    public long count(Criteria criteria) throws StoreException {
        
        try(Connection con = dataSource.getConnection()){
            
        }catch(SQLException ex){
            throw new StoreException(ex);
        }
    }

}
