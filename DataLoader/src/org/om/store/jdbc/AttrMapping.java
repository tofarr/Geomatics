package org.om.store.jdbc;

/**
 *
 * @author tofar
 */
public interface AttrMapping {

    void toSelectSql(StringBuilder sql);
    
    void toUpdateSql(StringBuilder sql);
}
