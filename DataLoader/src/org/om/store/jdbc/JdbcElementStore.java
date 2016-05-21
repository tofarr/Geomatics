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

    private AttrSet attrs;
    private final JdbcCreator creator;
    private final JdbcUpdater updater;
    private final JdbcReader reader;

    public JdbcElementStore(AttrSet attrs, JdbcCreator creator, JdbcUpdater updater, JdbcReader reader) {
        this.attrs = attrs;
        this.creator = creator;
        this.updater = updater;
        this.reader = reader;
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
        return reader.read(criteria, sorter, processor); NOT RIGHT
    }

    @Override
    public long count(Criteria criteria) throws StoreException {
        return reader.count(criteria);
    }

    @Override
    public ObjElement create(ObjElement element) throws StoreException {
        return creator.create(element);
    }

    @Override
    public long update(Criteria criteria, ObjElement element) throws StoreException {
        return updat
    }

    @Override
    public long remove(Criteria criteria) throws StoreException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void createAll(List<ObjElement> elements) throws StoreException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
