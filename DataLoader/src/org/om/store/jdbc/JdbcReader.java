
package org.om.store.jdbc;

import java.beans.Transient;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.om.attr.Attr;
import org.om.attr.AttrSet;
import org.om.criteria.All;
import org.om.criteria.Criteria;
import org.om.element.Element;
import org.om.element.ObjElement;
import org.om.element.ObjElementBuilder;
import org.om.sort.AttrSorter;
import org.om.sort.DescSorter;
import org.om.sort.Sorter;
import org.om.sort.ValueSorter;
import org.om.store.ElementProcessor;
import org.om.store.StoreException;

/**
 *
 * @author tofar
 */
public class JdbcReader {
    
    private final DataSource dataSource;
    private final String tableName;
    private final AttrSet attrs;
    private final Map<String, String> attr2Col;
    private final JdbcCriteriaHandler criteriaHandler;

    public JdbcReader(DataSource dataSource, String tableName, AttrSet attrs, Map<String, String> attr2Col, JdbcCriteriaHandler criteriaHandler) {
        this.dataSource = dataSource;
        this.tableName = tableName;
        this.attrs = attrs;
        this.attr2Col = attr2Col;
        this.criteriaHandler = criteriaHandler;
    }

    public boolean read(List<String> attrs, Criteria criteria, Sorter sorter, ElementProcessor processor) {
        AttrSet readAttrs = this.attrs.filter(attrs);
        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement stmt = con.prepareStatement(getSql(readAttrs, criteria, sorter))) {
                criteriaHandler.populate(1, criteria, stmt);
                try(ResultSet rs = stmt.executeQuery()){
                    while(rs.next()){
                        ObjElement element = parseRow(rs);
                        if(!processor.process(element)){
                            return false;
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException | IOException ex) {
            throw new StoreException(ex);
        }
    }
    
    public long count(Criteria criteria){
        try(Connection con = dataSource.getConnection()){
            try (PreparedStatement stmt = con.prepareStatement(getCountSql(criteria))) {
                criteriaHandler.populate(1, criteria, stmt);
                try(ResultSet rs = stmt.executeQuery()){
                    rs.next();
                    return rs.getLong(1);
                }
            }
        } catch (SQLException ex) {
            throw new StoreException(ex);
        }
    }

    public String getSql(AttrSet attrs, Criteria criteria, Sorter sorter) {
        StringBuilder str = new StringBuilder("SELECT ");
        boolean comma = false;
        for (Attr attr : attrs) {
            String col = attr2Col.get(attr.getName());
            if (comma) {
                str.append(',');
            } else {
                comma = true;
            }
            str.append(col);
        }
        str.append(" FROM ").append(tableName);
        criteriaHandler.buildSql(criteria, str);
        buildOrderSql(sorter, str);
        return str.toString();
    }  
    
    public String getCountSql(Criteria criteria) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ").append(tableName);
        if ((criteria != null) && (criteria != All.INSTANCE)) {
            sql.append(" WHERE ");
            criteriaHandler.buildSql(criteria, sql);
        }
        return sql.toString();
    }
    
    private ObjElement parseRow(ResultSet rs) throws SQLException, IOException {
        ObjElementBuilder builder = new ObjElementBuilder();
        int index = 1;
        for(Attr attr : attrs){
            switch(attr.getType()){
                case BOOLEAN:
                    builder.putBool(attr.getName(), rs.getBoolean(index++));
                    break;
                case NUMBER:
                    builder.putNum(attr.getName(), rs.getDouble(index++));
                    break;
                case STRING:
                    builder.putStr(attr.getName(), rs.getString(index++));
                    break;
                default:
                    try(Reader reader = rs.getCharacterStream(index++)){
                        if(reader != null){
                            Element element = Element.readStream(reader);
                            builder.put(attr.getName(), element);
                        }
                    }
            }
        }
        return builder.build();
    }

    public void buildOrderSql(Sorter sorter, StringBuilder sql) {
        if (sorter == null) {
            return;
        }
        if (sorter instanceof DescSorter) {
            buildOrderSql(((DescSorter) sorter).getSorter(), sql);
            sql.append(" DESC");
            return;
        } else if (sorter instanceof AttrSorter) {
            AttrSorter attrSorter = (AttrSorter) sorter;
            if (attrSorter.getSorter() == ValueSorter.INSTANCE) {
                String col = attr2Col.get(attrSorter.getAttrName());
                if (col == null) {
                    throw new IllegalArgumentException("Unknown attr : " + attrSorter.getAttrName());
                }
                sql.append(" ORDER BY ").append(col);
                return;
            }
        }
        throw new UnsupportedOperationException();
    }
}
