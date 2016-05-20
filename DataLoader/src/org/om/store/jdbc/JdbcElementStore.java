package org.om.store.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.om.attr.Attr;
import org.om.attr.AttrSet;
import org.om.criteria.Criteria;
import org.om.element.Element;
import org.om.element.ObjElement;
import org.om.sort.AttrSorter;
import org.om.sort.DescSorter;
import org.om.sort.Sorter;
import org.om.sort.ValueSorter;
import org.om.store.Capabilities;
import org.om.store.ElementProcessor;
import org.om.store.ElementStore;
import org.om.store.StoreException;

/**
 *
 * @author tofar
 */
public class JdbcElementStore implements ElementStore {

    private final DataSource dataSource;
    private final String tableName;
    private final AttrSet attrs;
    private final Map<String, String> attr2Col;

    public JdbcElementStore(DataSource dataSource, String tableName, AttrSet attrs,
            Map<String, String> attr2Col) {
        this.dataSource = dataSource;
        this.tableName = tableName;
        this.attrs = attrs;
        if (attr2Col == null) {
            attr2Col = new HashMap<>();
            for (int i = 0; i < attrs.size(); i++) {
                String a = attrs.byIndex(i).getName();
                attr2Col.put(a, a);
            }
        } else {
            for (int i = 0; i < attrs.size(); i++) {
                String attrName = attrs.byIndex(i).getName();
                if (!attr2Col.containsKey(attrName)) {
                    throw new IllegalArgumentException("Missing column mapping for : " + attrName);
                }
            }
        }
        this.attr2Col = attr2Col;

    }

    @Override
    public Capabilities getCapabilities() {
        return Capabilities.ALL;
    }

    @Override
    public AttrSet getAttrs() {
        return attrs;
    }

    @Override
    public boolean load(List<String> attrs, Criteria criteria, Sorter sorter, ElementProcessor processor) throws StoreException {
        AttrSet attrsToLoad = (attrs == null) ? this.attrs : this.attrs.filter(attrs);
        if (attrsToLoad.size() == 0) {
            throw new IllegalArgumentException("Must specify at least 1 attribute!");
        }
        StringBuilder str = new StringBuilder("SELECT ");
        for (int a = 0; a < attrsToLoad.size(); a++) {
            Attr attr = attrsToLoad.byIndex(a);
            if (a != 0) {
                str.append(',');
            }
            String colName = attr2Col.get(attr.getName());
            if (colName == null) {
                throw new IllegalArgumentException("Unknown attr : " + attr.getName());
            }
            str.append(colName);
        }
        str.append(" FROM ").append(tableName);
        if (criteria != null) {
            criteriaToSql(criteria, str);
        }
        sorterToSql(sorter, str);
        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement stmt = con.prepareStatement(str.toString())) {
                int index = populateStmtFromCriteria(criteria, 1, stmt);
                populateStmtFromSorter(sorter, index, stmt);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Element> map = new HashMap<>();
                        for (int i = 0; i < attrsToLoad.size();) {
                            Attr attr = attrsToLoad.byIndex(i);
                            Object obj = rs.getObject(++i);
                            Element element = toElement(obj, attr);
                            map.put(attr.getName(), element);
                        }
                        ObjElement element = ObjElement.valueOf(map);
                        if (!processor.process(element)) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException ex) {
            throw new StoreException(ex);
        }
    }

    @Override
    public long count(Criteria criteria) throws StoreException {
        StringBuilder str = new StringBuilder("SELECT COUNT(*) FROM ").append(tableName);
        if (criteria != null) {
            criteriaToSql(criteria, str);
        }

        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement stmt = con.prepareStatement(str.toString())) {
                populateStmtFromCriteria(criteria, 1, stmt);
                try (ResultSet rs = stmt.executeQuery()) {
                    rs.next();
                    return rs.getLong(1);
                }
            }
        } catch (SQLException ex) {
            throw new StoreException(ex);
        }
    }

    @Override
    public ObjElement create(ObjElement element) throws StoreException {
        element = attrs.filterElement(element);
        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement stmt = con.prepareStatement(getCreateSql())) {
                for (int a = 0; a < attrs.size(); a++) {
                    Attr attr = attrs.byIndex(a);
                    fromElement(element.getElement(attr.getName()), stmt);
                }
                stmt.executeUpdate();
            }
            //TODO : Generated keys
            return element;
        } catch (SQLException ex) {
            throw new StoreException(ex);
        }
    }

    @Override
    public long update(Criteria criteria, ObjElement element) throws StoreException {

    }

    @Override
    public long remove(Criteria criteria) throws StoreException {
        StringBuilder str = new StringBuilder("DELETE FROM ").append(tableName);
        if (criteria != null) {
            criteriaToSql(criteria, str);
        }

        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement stmt = con.prepareStatement(str.toString())) {
                populateStmtFromCriteria(criteria, 1, stmt);
                try (ResultSet rs = stmt.executeQuery()) {
                    rs.next();
                    return rs.getLong(1);
                }
            }
        } catch (SQLException ex) {
            throw new StoreException(ex);
        }
    }

    @Override
    public void createAll(List<ObjElement> elements) throws StoreException {
        try (Connection con = dataSource.getConnection()) {
            boolean autoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = con.prepareStatement(getCreateSql())) {
                    for (ObjElement element : elements) {
                        for (int a = 0; a < attrs.size(); a++) {
                            Attr attr = attrs.byIndex(a);
                            fromElement(element.getElement(attr.getName()), stmt);
                        }
                        stmt.addBatch();
                    }
                    //TODO : Generated keys
                    stmt.executeBatch();
                    con.commit();
                }
            } finally {
                con.setAutoCommit(autoCommit);
            }
        } catch (SQLException ex) {
            throw new StoreException(ex);
        }
    }

    private void criteriaToSql(Criteria criteria, StringBuilder str) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void sorterToSql(Sorter sorter, StringBuilder str) {
        if (sorter == null) {
            return;
        }
        if (sorter instanceof DescSorter) {
            sorterToSql(((DescSorter) sorter).getSorter(), str);
            str.append(" DESC");
            return;
        } else if (sorter instanceof AttrSorter) {
            AttrSorter attrSorter = (AttrSorter) sorter;
            if (attrSorter.getSorter() == ValueSorter.INSTANCE) {
                String col = attr2Col.get(attrSorter.getAttrName());
                if (col == null) {
                    throw new IllegalArgumentException("Unknown attr : " + attrSorter.getAttrName());
                }
                str.append(" ORDER BY ").append(col);
                return;
            }
        }
        throw new UnsupportedOperationException();
    }

    private int populateStmtFromCriteria(Criteria criteria, int i, PreparedStatement stmt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void populateStmtFromSorter(Sorter sorter, int index, PreparedStatement stmt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Element toElement(Object obj, Attr attr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String getCreateSql() {
        StringBuilder str = new StringBuilder("INSERT INTO ").append(tableName).append('(');
        char c = ' ';
        for (int a = 0; a < attrs.size(); a++) {
            Attr attr = attrs.byIndex(a);
            String col = attr2Col.get(attr);
            str.append(c).append(col);
            c = ',';
        }
        c = '(';
        str.append(") VALUES ");
        for (int a = 0; a < attrs.size(); a++) {
            str.append(c);
            str.append('?');
            c = ',';
        }
        str.append(")");
        return str.toString();
    }

    private void fromElement(Element element, PreparedStatement stmt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
