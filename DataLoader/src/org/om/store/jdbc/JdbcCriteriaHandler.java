package org.om.store.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import org.om.attr.AttrSet;
import org.om.criteria.And;
import org.om.criteria.Criteria;
import org.om.criteria.Equal;
import org.om.criteria.Length;
import org.om.criteria.Not;
import org.om.criteria.Or;
import org.om.criteria.object.Key;
import org.om.criteria.value.Greater;
import org.om.criteria.value.GreaterEq;
import org.om.criteria.value.Less;
import org.om.criteria.value.LessEq;
import org.om.criteria.value.StrContains;
import org.om.criteria.value.StrEndsWith;
import org.om.criteria.value.StrStartsWith;

/**
 *
 * @author tofar
 */
public class JdbcCriteriaHandler {

    private final AttrSet attrs;
    private final Map<String, String> attr2Col;

    public JdbcCriteriaHandler(AttrSet attrs, Map<String, String> attr2Col) {
        this.attrs = attrs;
        this.attr2Col = attr2Col;
    }

    public void buildSql(Criteria criteria, StringBuilder sql) {
        if (criteria instanceof And) {
            And and = (And)criteria;
            for(int c = 0; c < and.numCriteria(); c++){
                if(c > 0){
                    sql.append(" AND ");
                }
                sql.append('(');
                buildSql(and.getCriteria(c), sql);
                sql.append(')');
            }
        } else if (criteria instanceof Or) {
            Or or = (Or)criteria;
            sql.append('(');
            for(int c = 0; c < or.numCriteria(); c++){
                if(c > 0){
                    sql.append(" OR ");
                }
                sql.append('(');
                buildSql(or.getCriteria(c), sql);
                sql.append(')');
            }
            sql.append(')');
        } else if (criteria instanceof Not) {
            sql.append(" NOT (");
            buildSql(((Not)criteria).getCriteria(), sql);
            sql.append(')');
        } else if (criteria instanceof Key) {
            Key key = (Key)criteria;
            if(attr2Col.get(key.getKey()) == null){
                throw new IllegalArgumentException("Unknown attr : " + key.getKey());
            }
            Criteria c = key.getCriteria();
            if(c instanceof Equal){
                sql.append("=?");
            }else if(c instanceof Greater){
                sql.append(">?");
            }else if(c instanceof GreaterEq){
                sql.append(">=?");
            }else if(c instanceof Less){
                sql.append("<?");
            }else if(c instanceof LessEq){
                sql.append("<=?");
            }else if(c instanceof StrContains){
                sql.append(" LIKE ?");
            }else if(c instanceof StrEndsWith){
                sql.append(" LIKE ?");
            }else if(c instanceof StrStartsWith){
                sql.append(" LIKE ?");
            }
        }
    }

    public int populate(int index, Criteria criteria, PreparedStatement stmt) throws SQLException {
        if (criteria instanceof And) {
            And and = (And)criteria;
            for(int c = 0; c < and.numCriteria(); c++){
                index = populate(index, and.getCriteria(c), stmt);
            }
        } else if (criteria instanceof Or) {
            Or or = (Or)criteria;
            for(int c = 0; c < or.numCriteria(); c++){
                index = populate(index, or.getCriteria(c), stmt);
            }
        } else if (criteria instanceof Not) {
            index = populate(index, ((Not)criteria).getCriteria(), stmt);
        } else if (criteria instanceof Key) {
            Key key = (Key)criteria;
            if(attr2Col.get(key.getKey()) == null){
                throw new IllegalArgumentException("Unknown attr : " + key.getKey());
            }
            Criteria c = key.getCriteria();
            if(c instanceof Equal){
                JdbcCreator.populateCol(((Equal)c).getValue(), index++, stmt);
            }else if(c instanceof Greater){
                JdbcCreator.populateCol(((Greater)c).getValue(), index++, stmt);
            }else if(c instanceof GreaterEq){
                JdbcCreator.populateCol(((GreaterEq)c).getValue(), index++, stmt);
            }else if(c instanceof Less){
                JdbcCreator.populateCol(((Less)c).getValue(), index++, stmt);
            }else if(c instanceof LessEq){
                JdbcCreator.populateCol(((LessEq)c).getValue(), index++, stmt);
            }else if(c instanceof StrContains){
                String value = ((StrContains)c).getValue();
                if(value != null){
                    value = value.replace("%", "\\%");
                }
                stmt.setString(index++, value);
            }else if(c instanceof StrEndsWith){
                String value = ((StrEndsWith)c).getValue();
                if(value != null){
                    value = value.replace("%", "\\%");
                }
                stmt.setString(index++, value);
            }else if(c instanceof StrStartsWith){
                String value = ((StrStartsWith)c).getValue();
                if(value != null){
                    value = value.replace("%", "\\%");
                }
                stmt.setString(index++, value);
            }
        }
        return index;
    }
}
