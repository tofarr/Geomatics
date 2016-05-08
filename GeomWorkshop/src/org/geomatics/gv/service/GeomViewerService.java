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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.geomatics.geom.Rect;
import org.geomatics.gv.model.GeomViewerModel;
import org.geomatics.gv.model.LayerModel;
import org.geomatics.gv.model.LayerViewModel;
import org.geomatics.gv.service.WatcherService.PathListener;
import org.geomatics.util.ViewPoint;
import org.jayson.Jayson;
import org.jayson.PrettyPrintJaysonWriter;

/**
 *
 * @author tofarrell
 */
public class GeomViewerService implements AutoCloseable {

    private static final String STATE_FILE = "state.json";
    private final ArrayList<GeomViewerListener> listeners;
    private final Jayson jayson;
    private final WatcherService watcher;
    private final ScheduledExecutorService executor;
    private ScheduledFuture<?> save;
    private List<LayerViewModel> layers;
    private GeomViewerModel model;

    public GeomViewerService() {
        this.listeners = new ArrayList<>();
        this.jayson = Jayson.getInstance();
        this.watcher = new WatcherService();
        this.executor = new ScheduledThreadPoolExecutor(1);
    }
    
    public synchronized LayerViewModel[] getLayerViews(){
        LayerViewModel[] _layers = new LayerViewModel[layers.size()];
        for(int i = 0; i < _layers.length; i++){
            _layers[i] = layers.get(i);
        }
        return _layers;
    }

    public synchronized int numLayers() {
        loadLayers();
        return layers.size();
    }

    public synchronized LayerViewModel getLayerView(int index) {
        loadLayers();
        return layers.get(index);
    }

    public synchronized void setLayerView(int index, LayerModel layer) {
        loadLayers();
        LayerViewModel old = layers.get(index);
        if (old.path != null) {
            saveLayer(old.path, layer); // watching file will result in change bubbling up from file system
        } else {
            LayerViewModel layerView = new LayerViewModel(null, layer);
            layers.set(index, layerView);
            for (GeomViewerListener listener : listeners) {
                listener.onLayerUpdate(index, layerView);
            }
        }
    }

    public synchronized void setLayerView(int index, String path) {
        loadLayers();
        LayerViewModel old = layers.get(index);
        if (Objects.equals(old.path, path)) {
            return;
        }
        if (old.path != null) {
            unwatchPath(old.path);
        }
        if (path != null) {
            watchPath(path);
            LayerModel layer = loadLayer(path);
            layers.set(index, new LayerViewModel(path, layer));
            for (GeomViewerListener listener : listeners) {
                listener.onLayerUpdate(index, old);
            }
        }
        updateModelFromLayers();
    }
    
    public synchronized void setLayerView(int index, String path, LayerModel layer) {
        loadLayers();
        LayerViewModel old = layers.get(index);
        if (Objects.equals(old.path, path)) {
            return;
        }
        if (old.path != null) {
            unwatchPath(old.path);
        }
        if(layer == null){
            File file = new File(path);
            file.delete();
        }else{
            saveLayer(path, layer);
        }
        LayerViewModel layerView = new LayerViewModel(path, layer);
        layers.set(index, layerView);
        if (path != null) {
            watchPath(path);
        }
        for (GeomViewerListener listener : listeners) {
            listener.onLayerUpdate(index, layerView);
        }
        updateModelFromLayers();
    }

    public synchronized void addLayerView(LayerModel layer) {
        loadLayers();
        LayerViewModel layerView = new LayerViewModel(null, layer);
        int index = layers.size();
        layers.add(layerView);
        for (GeomViewerListener listener : listeners) {
            listener.onLayerUpdate(index, layerView);
        }
    }

    public synchronized void addLayerView(String path) {
        loadLayers();
        LayerModel layer = loadLayer(path);
        if (layer == null) {
            throw new ServiceException("No layer found at path : " + path);
        }
        watchPath(path);
        LayerViewModel layerView = new LayerViewModel(path, layer);
        int index = layers.size();
        layers.add(layerView);
        for (GeomViewerListener listener : listeners) {
            listener.onLayerUpdate(index, layerView);
        }
        updateModelFromLayers();
    }

    public synchronized void removeLayerView(int index, boolean purge){
        loadLayers();
        LayerViewModel layerView = layers.get(index);
        layers.remove(index);
        for (GeomViewerListener listener : listeners) {
            listener.onLayerUpdate(index, null);
        }
        if(layerView.path != null){
            updateModelFromLayers();
        }
        if(purge && (layerView.path != null)){
            File file = new File(layerView.path);
            file.delete();
        }
    }
    
    public synchronized Rect getBounds() {
        return loadModel().getBounds();
    }

    public synchronized void setBounds(Rect bounds) {
        GeomViewerModel _model = loadModel();
        if(bounds.equals(_model.getBounds())){
            return;
        }
        _model = _model.withBounds(bounds);
        updateModel(_model);
        for(GeomViewerListener listener : listeners){
            listener.onViewUpdate(bounds, _model.getViewPoint());
        }
    }

    public synchronized ViewPoint getViewPoint() {
        return loadModel().getViewPoint();
    }

    public synchronized void setViewPoint(ViewPoint viewPoint) {
        GeomViewerModel _model = loadModel();
        if(!_model.getViewPoint().equals(viewPoint)){
            _model = _model.withViewPoint(viewPoint);
             updateModel(_model);
             for(GeomViewerListener listener : listeners){
                 listener.onViewUpdate(_model.getBounds(), viewPoint);
             }
        }
    }

    private synchronized List<LayerViewModel> loadLayers() throws ServiceException {
        if (layers != null) {
            return layers;
        }
        GeomViewerModel model = loadModel();
        List<String> paths = model.getPaths();
        layers = new ArrayList<>();
        for (String path : paths) {
            LayerModel layer = loadLayer(path);
            layers.add(new LayerViewModel(path, layer));
        }
        return layers;
    }

    private synchronized GeomViewerModel loadModel() throws ServiceException {
        if (model != null) {
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
            Rect bounds = Rect.valueOf(100, 100, 740, 580);
            updateModel(GeomViewerModel.valueOf(bounds, ViewPoint.DEFAULT));
            return this.model;
        }
    }

    private synchronized  LayerModel loadLayer(String path) {
        try (Reader reader = new BufferedReader(new FileReader(path))) {
            LayerModel layer = jayson.parse(LayerModel.class, reader);
            return layer;
        } catch (Exception ex) { // maybe file was corrupt or no longer exists?
            for (GeomViewerListener listener : listeners) {
                listener.onError(ex);
            }
            return null;
        }
    }
    
    private synchronized  void saveLayer(String path, LayerModel layer) {
        try (Writer writer = new BufferedWriter(new FileWriter(path))) {
            jayson.render(layer, new PrettyPrintJaysonWriter(writer));
        } catch (Exception ex) {
            for (GeomViewerListener listener : listeners) {
                listener.onError(ex);
            }
        }
    }
    
    private synchronized  void updateModelFromLayers(){
        List<String> paths = new ArrayList<>();
        for(LayerViewModel layerView : layers){
            paths.add(layerView.path);
        }
        String[] pathArray = paths.toArray(new String[paths.size()]);
        GeomViewerModel _model = GeomViewerModel.valueOf(model.getBounds(), model.getViewPoint(), pathArray);
        updateModel(_model);
    }
    
    private synchronized void updateModel(GeomViewerModel model) {
        this.model = model;
        if (save != null) {
            save.cancel(false);
            save = null;
        }
        save = executor.schedule(saver, 3, TimeUnit.SECONDS);
    }

    private final Runnable saver = new Runnable() {
        @Override
        public void run() {
            File file = new File(STATE_FILE);
            try (Writer writer = new BufferedWriter(new FileWriter(file))) {
                jayson.render(model, new PrettyPrintJaysonWriter(writer));
            } catch (Exception ex) {
                for (GeomViewerListener listener : listeners) {
                    listener.onError(ex);
                }
            }
        }
    };

    public synchronized void addListener(GeomViewerListener listener) {
        listeners.add(listener);
    }

    public synchronized void removeListener(GeomViewerListener listener) {
        listeners.remove(listener);
    }

    private void watchPath(String path) {
        if (path != null) {
            watcher.addListener(new File(path).toPath(), listener);
        }
    }

    private void unwatchPath(String path) {
        if (path != null) {
            watcher.removeListener(new File(path).toPath(), listener);
        }
    }

    private final PathListener listener = new PathListener() {
        @Override
        public void onUpdate(Path path) {
            for(int i = 0; i < layers.size(); i++){
                LayerViewModel layerView = layers.get(i);
                if (new File(layerView.path).toPath().equals(path)) {
                    LayerModel layer = loadLayer(layerView.path);
                    layerView = new LayerViewModel(layerView.path, layer);
                    layers.set(i, layerView);
                    for(GeomViewerListener listener : listeners){
                        listener.onLayerUpdate(i, layerView);
                    }
                }
            }
        }

        @Override
        public void onDelete(Path path) {
            onUpdate(path);
        }
    };

    @Override
    public void close() throws Exception {
        if (save != null) {
            save.cancel(false);
            save = null;;
        }
        saver.run();
        executor.shutdown();
        watcher.close();
        for (GeomViewerListener listener : listeners) {
            listener.onClose();
        }
        listeners.clear();
    }

    public interface GeomViewerListener {

        void onViewUpdate(Rect bounds, ViewPoint viewPoint);

        void onLayerUpdate(int index, LayerViewModel layer);

        void onError(Exception ex);

        void onClose();

    }
}
