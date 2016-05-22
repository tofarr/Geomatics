package org.om.store.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.om.criteria.All;
import org.om.criteria.Criteria;
import org.om.store.StoreException;

/**
 *
 * @author tofar
 */
public class JdbcRemover {

    private final DataSource dataSource;
    private final String tableName;
    private final JdbcCriteriaHandler criteriaHandler;

    public JdbcRemover(DataSource dataSource, String tableName, JdbcCriteriaHandler criteriaHandler) {
        this.dataSource = dataSource;
        this.tableName = tableName;
        this.criteriaHandler = criteriaHandler;
    }

    public long remove(Criteria criteria) {
        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement stmt = con.prepareStatement(getSql(criteria))) {
                populate(criteria, stmt);
                return stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new StoreException(ex);
        }
    }

    public void populate(Criteria criteria, PreparedStatement stmt) throws SQLException {
        criteriaHandler.populate(1, criteria, stmt);
    }

    public String getSql(Criteria criteria) {
        StringBuilder sql = new StringBuilder("DELETE FROM ").append(tableName);
        if ((criteria != null) && (criteria != All.INSTANCE)) {
            sql.append(" WHERE ");
            criteriaHandler.buildSql(criteria, sql);
        }
        return sql.toString();
    }
}
