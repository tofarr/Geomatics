package org.geomatics.gv.modelold;

import org.geomatics.gv.service.WatcherService;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geomatics.geom.Geom;
import org.geomatics.gv.beans.GeomLayer;
import org.geomatics.gv.beans.GeomLayer.GeomLayerListener;
import org.geomatics.gv.beans.GeomViewer;
import org.geomatics.gv.beans.GeomViewer.GeomViewerListener;
import org.geomatics.util.View;
import org.jayson.Jayson;

/**
 *
 * @author tofarrell
 */
public class GeomViewerService {

    private static final Logger LOG = Logger.getLogger(GeomViewerService.class.getName());
    private final List<GeomViewerServiceListener> listeners;
    private final WatcherService watcherService;
    private GeomViewer geomViewer;
    private boolean ignoreGeomUpdate;

    public GeomViewerService() {
        this.listeners = new ArrayList<>();
        this.watcherService = new WatcherService();
    }

    public GeomViewer getGeomViewer() {
        return geomViewer;
    }

    public void setGeomViewer(GeomViewer geomViewer) {
        if (this.geomViewer == geomViewer) {
            return;
        }
        if (this.geomViewer != null) {
            disconnect(this.geomViewer);
        }
        this.geomViewer = geomViewer;
        if (geomViewer != null) {
            connect(geomViewer);
        }
        onUpdateMeta();
        onUpdateView();
    }

    public void addListener(GeomViewerServiceListener listener) {
        listeners.add(listener);
    }

    public void removeListener(GeomViewerServiceListener listener) {
        listeners.remove(listener);
    }

    private void disconnect(GeomViewer geomViewer) {
        for (int i = 0; i < geomViewer.numLayers(); i++) {
            geomViewer.getLayer(i).removeListener(layerListener);
        }
        geomViewer.removeListener(viewerListener);
    }

    private void connect(GeomViewer geomViewer) {
        geomViewer.addListener(viewerListener);
        for (int i = 0; i < geomViewer.numLayers(); i++) {
            GeomLayer layer = geomViewer.getLayer(i);
            layer.addListener(layerListener);
            populateLayerGeom(layer);
            String path = layer.getPath();
            if(path != null){
                
            }
        }
    }
    
    
    private void addListenerToPath(GeomLayer layer, String path){
        
    }
    
    private void removeListenerFromPath(GeomLayer layer, String path){
        
    }

    private void onUpdateMeta() {
        for (GeomViewerServiceListener listener : listeners) { // This should update the screen
            listener.metaChanged();
        }
        System.out.println("TODO: Save bean in 500 millis");
    }

    private void onUpdateView() {
        for (GeomViewerServiceListener listener : listeners) { // This should update the screen
            listener.viewChanged();
        }
        if(!ignoreGeomUpdate){
            System.out.println("TODO: Save bean in 500 millis");
        }
    }

    private void populateLayerGeom(GeomLayer layer) {
        Geom ret = layer.getGeom();
        if (ret != null) {
            return;
        }
        String path = layer.getPath();
        if (path == null) {
            return;
        }
        try (Reader reader = new BufferedReader(new FileReader(path))) {
            Jayson jayson = Jayson.getInstance();
            Geom geom = jayson.parse(Geom.class, reader);
            ignoreGeomUpdate = true;
            try{
                layer.setGeom(geom);
            }finally{
                ignoreGeomUpdate = false;
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error loading resource : " + path, ex);
            for (GeomViewerServiceListener listener : listeners) { // This should update the screen
                listener.error(ex);
            }
        }
    }

    private final GeomViewerListener viewerListener = new GeomViewerListener() {
        @Override
        public void viewUpdated(View old, View view) {
            onUpdateView();
        }

        @Override
        public void layerAdded(GeomLayer layer, int index) {
            layer.addListener(layerListener);
            populateLayerGeom(layer);
            onUpdateMeta();
            onUpdateView();
        }

        @Override
        public void layerRemoved(GeomLayer layer, int index) {
            onUpdateMeta();
            onUpdateView();
        }

    };

    private final GeomLayerListener layerListener = new GeomLayerListener() {
        @Override
        public void titleChanged(GeomLayer layer, String oldTitle, String newTitle) {
            onUpdateMeta();
        }

        @Override
        public void geomChanged(GeomLayer layer, Geom oldGeom, Geom newGeom) {
            onUpdateView();
        }

        @Override
        public void pathChanged(GeomLayer layer, String oldPath, String newPath) {
            if(layer.getGeom() == null){ // load?
                
            } else {
                saveLayerGeom
            }
            int x = "attempt to save and load";
            
            onUpdateView();
        }

        @Override
        public void styleChanged(GeomLayer layer) {
            onUpdateView();
        }

    };

    interface GeomViewerServiceListener {

        void metaChanged();

        void viewChanged();

        void error(Exception ex);
    }

}
