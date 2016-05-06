package org.geomatics.gv.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.geomatics.util.View;
import org.jayson.parser.StaticFactory;

/**
 *
 * @author tofarrell
 */
public class GeomViewerModel {

    private static final Logger LOG = Logger.getLogger(GeomViewerModel.class.getName());

    private final View view;
    private final List<String> paths;

    private GeomViewerModel(View view, List<String> paths) {
        this.view = view;
        this.paths = paths;
    }

    @StaticFactory({"view", "paths"})
    public static GeomViewerModel valueOf(View view, String... paths) throws NullPointerException {
        if (view == null) {
            throw new NullPointerException("view must not be null");
        }
        List<String> pathList = new ArrayList<>();
        for (String path : paths) {
            if (pathList.contains(path)) {
                LOG.info("Duplicate path in index : "+path);
            }else{
                File file = new File(path);
                if(file.exists() && file.canRead()){
                    pathList.add(path);
                }else{
                    LOG.info("Unreadable path in index "+path);
                }
            }
        }
        return new GeomViewerModel(view, Collections.unmodifiableList(pathList));
    }

    public final int numPaths() {
        return paths.size();
    }

    public String getPath(int index) throws IndexOutOfBoundsException {
        return paths.get(index);
    }

    public View getView() {
        return view;
    }

    public List<String> getPaths() {
        return paths;
    }

    public GeomViewerModel withView(View view) {
        if (view == null) {
            throw new NullPointerException("view must not be null");
        }
        return new GeomViewerModel(view, paths);
    }
    
    public GeomViewerModel withExtraPath(String path){
        int index = paths.indexOf(path);
        if(index >= 0){
            return this;
        }
        ArrayList<String> newPaths = new ArrayList<>(paths);
        newPaths.add(path);
        return new GeomViewerModel(view, Collections.unmodifiableList(newPaths));
    }
    
    public GeomViewerModel withoutPath(String path){
        int index = paths.indexOf(path);
        if(index < 0){
            return this;
        }
        ArrayList<String> newPaths = new ArrayList<>(paths);
        newPaths.remove(index);
        return new GeomViewerModel(view, Collections.unmodifiableList(newPaths));
    }
    
    public int indexOf(String path){
        return paths.indexOf(path);
    }
    
    public List<String> getPathsNotInOther(GeomViewerModel other){
        ArrayList<String> newPaths = new ArrayList<>(paths);
        newPaths.removeAll(other.paths);
        return newPaths;
    }
}
