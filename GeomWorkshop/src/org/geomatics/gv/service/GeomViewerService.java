package org.geomatics.gv.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import org.geomatics.geom.Rect;
import org.geomatics.gv.model.GeomViewerModel;
import org.geomatics.gv.model.LayerModel;
import org.geomatics.gv.service.WatcherService.PathListener;
import org.geomatics.util.View;
import org.jayson.Jayson;
import org.jayson.PrettyPrintJaysonWriter;

/**
 *
 * @author tofarrell
 */
public class GeomViewerService {

    private static final String STATE_FILE = "state.json";
    private final ArrayList<GeomViewerListener> listeners;
    private final Jayson jayson;
    private final WatcherService watcher;
    private GeomViewerModel model;
    

    public GeomViewerService() {
        this.listeners = new ArrayList<>();
        this.jayson = Jayson.getInstance();
        this.watcher = new WatcherService();
    }

    public GeomViewerModel loadView() throws ServiceException {
        if(model != null){
            return model;
        }
        File file = new File(STATE_FILE);
        if (file.exists() && file.canRead()) {
            try (Reader reader = new BufferedReader(new FileReader(file))) {
                model = jayson.parse(GeomViewerModel.class, reader);
                for (int p = model.numPaths(); p-- > 0;) {
                    watchPath(model.getPath(p));
                }
                return model;
            } catch (Exception ex) {
                throw new ServiceException("Error loading state", ex);
            }
        } else {
            View view = new View(Rect.valueOf(100, 100, 740, 580), 640, 480);
            model = GeomViewerModel.valueOf(view);
            return model;
        }
    }

    public void storeView(GeomViewerModel model) throws ServiceException {
        if(Objects.equals(this.model, model)){
            return;
        }
        File file = new File(STATE_FILE);
        try (Writer writer = new BufferedWriter(new FileWriter(file))) {
            jayson.render(model, new PrettyPrintJaysonWriter(writer));
            for (String path : model.getPathsNotInOther(this.model)) {
                watchPath(path);
            }
            for (String path : this.model.getPathsNotInOther(model)) {
                unwatchPath(path);
            }
            this.model = model;
            for(GeomViewerListener listener : listeners){
                listener.viewUpdated();
            }
        } catch (Exception ex) {
            throw new ServiceException("Error loading state", ex);
        }
    }

    public LayerModel loadLayer(int index) throws ServiceException {
        String path = model.getPath(index);
        File file = new File(path);
        if ((!file.exists() && file.canRead())) {
            return null;
        }
        try (Reader reader = new BufferedReader(new FileReader(file))) {
            LayerModel ret = jayson.parse(LayerModel.class, reader);
            return ret;
        } catch (Exception ex) {
            throw new ServiceException("Error loading state", ex);
        }
    }

    public void storeLayer(int index, LayerModel layer) throws ServiceException {
        String path = model.getPath(index);
        try (Writer writer = new BufferedWriter(new FileWriter(path))) {
            jayson.render(layer, new PrettyPrintJaysonWriter(writer));
            for(GeomViewerListener listener : listeners){
                listener.layerUpdate(path);
            }
        } catch (Exception ex) {
            throw new ServiceException("Error loading state", ex);
        }
    }

    public void storeLayer(String path, LayerModel layer) throws ServiceException {
        int index = model.indexOf(path);
        if (index < 0) {
            GeomViewerModel newModel = model.withExtraPath(path);
            storeView(newModel);
            index = newModel.indexOf(path);
        }
        storeLayer(index, layer);
    }

    public void removeLayer(String path) {
        File file = new File(path);
        file.delete();
        for(GeomViewerListener listener : listeners){
            listener.layerUpdate(path);
        }
    }

    public void addListener(GeomViewerListener listener) {
        listeners.add(listener);
    }

    public void removeListener(GeomViewerListener listener) {
        listeners.remove(listener);
    }
    
    private void watchPath(String path){
        watcher.addListener(new File(path).toPath(), listener);
    }
    
    private void unwatchPath(String path){
        watcher.removeListener(new File(path).toPath(), listener);
    }
    
    private final PathListener listener = new PathListener(){
        @Override
        public void onUpdate(Path path) {
            for(int i = model.numPaths(); i-- > 0;){
                String pathStr = model.getPath(i);
                if(new File(pathStr).toPath().equals(path)){
                    
                    break;
                }
            }
        }

        @Override
        public void onDelete(Path path) {
            onUpdate(path);
        }
    };

    public interface GeomViewerListener {

        void viewUpdated();

        void layerUpdate(String path);

    }
}
