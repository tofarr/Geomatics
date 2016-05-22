package org.om.store.jdbc;

import java.beans.Transient;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import javax.sql.DataSource;
import org.om.criteria.All;
import org.om.criteria.Criteria;
import org.om.element.ObjElement;
import org.om.store.StoreException;

/**
 *
 * @author tofar
 */
public class JdbcUpdater {

    private final DataSource dataSource;
    private final String tableName;
    private final Map<String, String> attr2Col;
    private final JdbcCriteriaHandler criteriaHandler;

    public JdbcUpdater(DataSource dataSource, String tableName, Map<String, String> attr2Col, JdbcCriteriaHandler criteriaHandler) {
        this.dataSource = dataSource;
        this.tableName = tableName;
        this.attr2Col = attr2Col;
        this.criteriaHandler = criteriaHandler;
    }

    public long update(Criteria criteria, ObjElement obj) {
        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement stmt = con.prepareStatement(getSql(obj, criteria))) {
                populate(criteria, obj, stmt);
                return stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new StoreException(ex);
        }
    }
    
    public void populate(Criteria criteria, ObjElement obj, PreparedStatement stmt) throws SQLException {
        int index = JdbcCreator.populate(obj, attr2Col.keySet(), stmt);
        criteriaHandler.populate(index, criteria, stmt);
    }

    public String getSql(ObjElement element, Criteria criteria) {
        StringBuilder sql = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
        boolean comma = false;
        for(String key : element){
            String col = attr2Col.get(key);
            if(col != null){ // ignore non cols
                if (comma) {
                    sql.append(',');
                } else {
                    comma = true;
                }
                sql.append(col);
            }
        }
        if ((criteria != null) && (criteria != All.INSTANCE)) {
            sql.append(" WHERE ");
            criteriaHandler.buildSql(criteria, sql);
        }
        return sql.toString();
    }
}
