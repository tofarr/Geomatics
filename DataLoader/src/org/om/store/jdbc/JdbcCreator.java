package org.om.store.jdbc;

import java.beans.Transient;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.om.element.BoolElement;
import org.om.element.Element;
import org.om.element.NumElement;
import org.om.element.ObjElement;
import org.om.element.StrElement;
import org.om.store.StoreException;

/**
 *
 * @author tofar
 */
public class JdbcCreator {

    private final DataSource dataSource;
    private final String tableName;
    private final String keyAttr;
    private final Map<String, String> attr2Col;
    private transient volatile String sql;

    public JdbcCreator(DataSource dataSource, String tableName, String keyAttr, Map<String, String> attr2Col) {
        this.dataSource = dataSource;
        this.tableName = tableName;
        this.keyAttr = keyAttr;
        this.attr2Col = attr2Col;
    }

    public ObjElement create(ObjElement element) throws StoreException {
        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement stmt = con.prepareStatement(getSql(), Statement.RETURN_GENERATED_KEYS)) {
                populate(element, attr2Col.keySet(), stmt);
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    rs.next();
                    long key = rs.getLong(1);
                    ObjElement ret = element.putElement(keyAttr, NumElement.valueOf(key));
                    return ret;
                }
            }
        } catch (SQLException ex) {
            throw new StoreException(ex);
        }
    }

    public void createAll(List<ObjElement> elements) {
        try (Connection con = dataSource.getConnection()) {
            boolean autoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
            try (PreparedStatement stmt = con.prepareStatement(getSql(), Statement.RETURN_GENERATED_KEYS)) {
                for (ObjElement element : elements) {
                    populate(element, attr2Col.keySet(), stmt);
                    stmt.addBatch();
                }
                stmt.executeBatch();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    int index = 0;
                    while (rs.next()) {
                        rs.next();
                        long key = rs.getLong(1);
                        ObjElement element = elements.get(index);
                        element = element.putElement(keyAttr, NumElement.valueOf(key));
                        elements.set(index++, element);
                    }
                }
            } finally {
                con.setAutoCommit(autoCommit);
            }
        } catch (SQLException ex) {
            throw new StoreException(ex);
        }
    }

    @Transient
    public String getSql() {
        String ret = sql;
        if (ret != null) {
            return ret;
        }
        StringBuilder str = new StringBuilder("INSERT INTO ").append(tableName);
        char c = '(';

        for (String colName : attr2Col.values()) {
            str.append(c);
            c = ',';
            str.append(colName);
        }
        str.append(") VALUES ");
        c = '(';
        int size = attr2Col.size();
        for (int s = size; s-- > 0;) {
            str.append(c);
            c = ',';
            str.append('?');
        }
        str.append(')');
        ret = str.toString();
        sql = ret;
        return ret;
    }

    static int populate(ObjElement element, Collection<String> attrs, PreparedStatement stmt) throws SQLException {
        int index = 1;
        for (String attr : attrs) {
            Element e = element.getElement(attr);
            if (e == null) {
                stmt.setObject(index++, null);
            } else {
                populateCol(e, index++, stmt);
            }
        }
        return index;
    }
    
    static void populateCol(Element e, int index, PreparedStatement stmt) throws SQLException {
        if (e == null) {
            stmt.setObject(index, null);
        } else {
            switch (e.getType()) {
                case BOOLEAN:
                    stmt.setBoolean(index, ((BoolElement) e).isBool());
                    break;
                case NUMBER:
                    stmt.setDouble(index, ((NumElement) e).getNum());
                    break;
                case STRING:
                    stmt.setString(index, ((StrElement) e).getStr());
                    break;
                default:
                    StringBuilderReader str = new StringBuilderReader(new StringBuilder());
                    e.toStream(false, str.getStr());
                    stmt.setCharacterStream(index, str);
            }
        }
    }
}
