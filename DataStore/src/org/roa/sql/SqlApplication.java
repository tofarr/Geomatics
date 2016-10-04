
package org.roa.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import javax.sql.DataSource;
import org.roa.Application;
import org.roa.ResourceException;

/**
 *
 * @author tofarr
 */
public class SqlApplication implements Application {
    
    private final DataSourceFactory dataSourceFactory;
    private final Map<String, SqlWorkspace> workspaces;
    private final DataSource dataSource;
    
    public SqlApplication(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
        this.dataSource = dataSourceFactory.create();
        try(Connection con = dataSource.getConnection()){
            try(Statement stmt = con.createStatement()){
                try(ResultSet rs = stmt.executeQuery("select * from workspaces")){
                 
                    //get entitlements for workspace - or do we store this in a bag in the workspace?
                    
                    //
                }
            }
            
            //our options here are:
            
            //follow the sql standard - normalize data
            
            //an array means a link table
            
            //queries are done using joins...
            
            //e.g.: 
            
            // workspace => { 
               //entitlements: [{groupId: (long), privledge: (int)]
            //}
            
            // maps to workspace (id, name)
            
            // workspace_entitlements
            
            //workspace_entitlements_
            
        }catch(SQLException ex){
            throw new ResourceException(ex);
        }
    }
}
