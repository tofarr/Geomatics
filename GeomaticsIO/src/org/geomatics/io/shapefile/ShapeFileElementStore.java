package org.geomatics.io.shapefile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.geomatics.io.shapefile.dbf.DbfField;
import org.geomatics.io.shapefile.dbf.DbfHeader;
import org.geomatics.io.shapefile.shp.ShapeType;
import org.geomatics.io.shapefile.shp.ShpHeader;
import org.om.attr.Attr;
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
public class ShapeFileElementStore implements ElementStore {

    private final String path;
    private final String geomAttr;
    private final DbfHeader dbfHeader;
    private final ShpHeader shpHeader;

    public ShapeFileElementStore(String path, String geomAttr) {
        int index = path.lastIndexOf('.');
        int index2 = path.lastIndexOf(File.separatorChar);
        if (index > index2) {
            path = path.substring(0, index);
        }
        this.path = path;
        this.geomAttr = geomAttr;
        //this.geomReader = new GeomReader(new ShpOnlyReader(new BufferedInputStream(new FileInputStream(path + ".shp"))));
        try(InputStream in = new BufferedInputStream(new FileInputStream(path + ".dbf"))) {
            dbfHeader = DbfHeader.read(in);
        } catch (IOException ex) {
            throw new StoreException("Error reading dbf", ex);
        }
        try(InputStream in = new BufferedInputStream(new FileInputStream(path + ".shp"))) {
            shpHeader = ShpHeader.read(in);
        } catch (IOException ex) {
            throw new StoreException("Error reading dbf", ex);
        }
        //derive a criteria from shpHeader.shapeType
        
        //Derive attrs from dbfHeader.getFields()
        List<Attr> attrs = new ArrayList<>();
        for(DbfField field : dbfHeader.getFields()){
            field.getName();
            field.type // use this to convert between json and db, add criteria to attrs where possible.
        }

    }
    
    public ShapeFileElementStore(String path, String geomAttr, AttrSet attrs, ShapeType shapeType) {
        
    }
    
    @Override
    public Capabilities getCapabilities() {
        return Capabilities.ALL;
    }

    @Override
    public AttrSet getAttrs() {
        return 
    }

    @Override
    public boolean load(List<String> attrs, Criteria criteria, Sorter sorter, ElementProcessor processor) throws StoreException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long count(Criteria criteria) throws StoreException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ObjElement create(ObjElement element) throws StoreException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long update(Criteria criteria, ObjElement element) throws StoreException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
