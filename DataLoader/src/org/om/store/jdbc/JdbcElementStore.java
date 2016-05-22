package org.om.store.jdbc;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.om.attr.AttrSet;
import org.om.criteria.Criteria;
import org.om.element.ObjElement;
import org.om.sort.Sorter;
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
    private final JdbcRemover remover;

    public JdbcElementStore(AttrSet attrs, JdbcCreator creator, JdbcUpdater updater, JdbcReader reader, JdbcRemover remover) {
        this.attrs = attrs;
        this.creator = creator;
        this.updater = updater;
        this.reader = reader;
        this.remover = remover;
    }
    
    public static JdbcElementStore valueOf(String tableName, String keyAttr, AttrSet attrs, Map<String, String> attr2Col, DataSource dataSource) throws SQLException {
        JdbcCriteriaHandler handler = new JdbcCriteriaHandler(attrs, attr2Col);
        JdbcCreator creator = new JdbcCreator(dataSource, tableName, keyAttr, attr2Col);
        JdbcUpdater updater = new JdbcUpdater(dataSource, tableName, attr2Col, handler);
        JdbcReader reader = new JdbcReader(dataSource, tableName, attrs, attr2Col, handler);
        JdbcRemover remover = new JdbcRemover(dataSource, tableName, handler);
        return new JdbcElementStore(attrs, creator, updater, reader, remover);
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
        return reader.read(attrs, criteria, sorter, processor);
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
        return updater.update(criteria, element);
    }

    @Override
    public long remove(Criteria criteria) throws StoreException {
        return remover.remove(criteria);
    }

    @Override
    public void createAll(List<ObjElement> elements) throws StoreException {
        creator.createAll(elements);
    }

}
