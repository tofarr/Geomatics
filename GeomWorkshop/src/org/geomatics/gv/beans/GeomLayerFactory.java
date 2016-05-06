package org.geomatics.gv.beans;

import java.beans.ConstructorProperties;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import org.jayson.Jayson;

/**
 *
 * @author tofarrell
 */
public class GeomLayerFactory {

    private final String path;

    @ConstructorProperties({"path","layer"})
    public GeomLayerFactory(String path) {
        if(path == null){
            throw new NullPointerException();
        }
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public GeomLayer load() throws IOException{
        if(path == null){
            return null;
        }
        File file = new File(path);
        if(!file.exists()){
            return null;
        }
        Jayson jayson = Jayson.getInstance();
        try(Reader reader = new BufferedReader(new FileReader(path))){
            GeomLayer ret = jayson.parse(GeomLayer.class, reader);
            return ret;
        }
    }
    
    public void store(GeomLayer layer){
        
    }
    
    public void remove(){
        File file = new File(path);
        file.delete();
    }
}
