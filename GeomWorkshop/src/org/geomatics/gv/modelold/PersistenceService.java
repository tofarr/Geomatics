package org.geomatics.gv.modelold;

import org.geomatics.gv.service.ServiceException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import org.geomatics.gv.beans.GeomViewer;
import org.jayson.Jayson;
import org.jayson.JaysonReader;
import org.jayson.PrettyPrintJaysonWriter;

/**
 *
 * @author tofar
 */
public class PersistenceService {

    private static final File STATE_FILE = new File("state.json");

    public GeomViewer load() throws ServiceException {
        if (!STATE_FILE.exists()) {
            return null;
        }
        Jayson jayson = Jayson.getInstance();
        try(JaysonReader reader = new JaysonReader(new FileReader(STATE_FILE))){
            GeomViewer ret = jayson.parse(GeomViewer.class, reader);
            return ret;
        }catch(Exception ex){
            throw new ServiceException("Error loading", ex);
        }
    }

    public void store(GeomViewer model) throws ServiceException {
        Jayson jayson = Jayson.getInstance();
        try(PrettyPrintJaysonWriter writer = new PrettyPrintJaysonWriter(new FileWriter(STATE_FILE))){
            jayson.render(model, writer);
        }catch(Exception ex){
            throw new ServiceException("Error loading", ex);
        }
    }
}
