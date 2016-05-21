package org.om.store.jdbc;

import java.beans.ConstructorProperties;
import java.beans.Transient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author tofar
 */
public class TableAttrMapping {

    private final String tableName;
    private final Map<String, List<String>> attr2Col;
    private final Map<String, List<String>> col2Attr;

    @ConstructorProperties({"tableName", "attrs", "cols"})
    public TableAttrMapping(String tableName, String[] attrs, String[] cols) {
        if (attrs.length != cols.length) {
            throw new IllegalArgumentException("Should be same number of attrs and colNames. (" + attrs.length + " attrs, " + cols.length + " cols)");
        }
        this.tableName = tableName;
        HashMap<String,List<String>> _attr2Col = new HashMap<>();
        HashMap<String,List<String>> _col2Attr = new HashMap<>();
        for (int i = attrs.length; i-- > 0;) {

            String attr = attrs[i];
            String col = cols[i];

            List<String> colList = _attr2Col.get(attr);
            if (colList == null) {
                colList = new ArrayList<>();
                _attr2Col.put(attr, colList);
            }
            colList.add(col);

            List<String> attrList = _col2Attr.get(col);
            if (attrList == null) {
                attrList = new ArrayList<>();
                _attr2Col.put(col, attrList);
            }
            attrList.add(attr);
        }
        for (Entry<String, List<String>> entry : _attr2Col.entrySet()) {
            entry.setValue(Collections.unmodifiableList(entry.getValue()));
        }
        for (Entry<String, List<String>> entry : _col2Attr.entrySet()) {
            entry.setValue(Collections.unmodifiableList(entry.getValue()));
        }
        this.attr2Col = Collections.unmodifiableMap(_attr2Col);
        this.col2Attr = Collections.unmodifiableMap(_col2Attr);
    }

    public String getTableName() {
        return tableName;
    }

    public List<String> getColNames(String attrName) {
        return attr2Col.get(attrName);
    }

    public List<String> getAttrNames(String colName) {
        return col2Attr.get(colName);
    }
    
    @Transient
    public Map<String,List<String>> getCol2Attr(){
        return col2Attr;
    }
    
    @Transient
    public Map<String,List<String>> getAttr2Col(){
        return attr2Col;
    }
    
    public String[] getAttrs(){
        int size = Math.max(attr2Col.size(), col2Attr.size());
        String[] ret = new String[size];
        int i = 0;
        for(Entry<String,List<String>> entry : attr2Col.entrySet()){
            List<String> values = entry.getValue();
            for(int j = values.size(); j-- > 0;){
                ret[i++] = entry.getKey();
            }
        }
        return ret;
    }
    
    public String[] getCols(){
        int size = Math.max(attr2Col.size(), col2Attr.size());
        String[] ret = new String[size];
        int i = 0;
        for(List<String> cols : attr2Col.values()){
            for(String col : cols){
                ret[i++] = col;
            }
        }
        return ret;
    }

}
