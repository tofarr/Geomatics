package org.geomatics.gv.service;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tofarrell
 */
public class WatcherService implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(WatcherService.class.getName());

    private WatchService watchService;
    private final ConcurrentHashMap<Path, PathWatch> byPath = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<WatchKey, PathWatch> byKey = new ConcurrentHashMap<>();
    private volatile boolean doRun;
    private volatile boolean running;

    public synchronized void addListener(Path path, PathListener listener) throws ServiceException {
        try {
            if (watchService == null) {
                watchService = FileSystems.getDefault().newWatchService();
            }
            path = path.toAbsolutePath();
            Path parent = path.getParent();
            PathWatch watch = byPath.get(parent);
            if (watch == null) {
                WatchKey key = parent.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
                watch = new PathWatch(key, parent);
                byPath.put(parent, watch);
                byKey.put(key, watch);
            }
            watch.addListener(path, listener);
            if (!running) {
                doRun = true;
                new Thread(runnable).start();
            }
        } catch (Exception ex) {
            throw new ServiceException("Error adding Listener", ex);
        }
    }

    public synchronized void removeListener(Path path, PathListener listener) {
        path = path.toAbsolutePath();
        Path parent = path.getParent();
        PathWatch watch = byPath.get(parent);
        if (watch == null) {
            return;
        }
        watch.removeListener(path, listener);
        if (watch.listeners.isEmpty()) {
            byKey.remove(watch.key);
            byPath.remove(watch.path);
        }
        if (byKey.isEmpty()) {
            doRun = false;
        }
    }
    
    public synchronized void close() throws IOException{
        doRun = false;
        watchService.close();
        byKey.clear();
        byPath.clear();
    }

    private synchronized boolean check() {
        if (doRun) {
            running = true;
            return true;
        } else {
            running = false;
            return false;
        }
    }

    private synchronized void remove(PathWatch watch) {
        byKey.remove(watch.key);
        byPath.remove(watch.path);
        if (byKey.isEmpty()) {
            doRun = false;
            try {
                watchService.close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            ArrayList<Path> subPaths = new ArrayList<>();
            ArrayList<PathListener> listeners = new ArrayList<>();
            while (check()) {

                // wait for key to be signalled
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException x) {
                    return;
                }
                PathWatch watch = byKey.get(key);
                if (watch == null) {
                    LOG.severe(MessageFormat.format("WatchKey not recognized: {0}", key));
                    continue;
                }

                subPaths.clear();
                listeners.clear();
                synchronized (watch) {
                    subPaths.addAll(watch.subPaths);
                    listeners.addAll(watch.listeners); // defensive copy!
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
                
                for (WatchEvent<?> event : key.pollEvents()) {
                    if(event.context() instanceof Path){
                        Path path = (Path) event.context();
                        path = path.toAbsolutePath();
                        for(int i = listeners.size(); i-- > 0;){
                            if(subPaths.get(i).equals(path)){
                                Kind kind = event.kind();
                                if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                                    listeners.get(i).onUpdate(path);
                                } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                                    listeners.get(i).onDelete(path);
                                }
                            }
                        }
                        
                    }
                }

                // reset key and remove from set if directory no longer accessible
                if (!key.reset()) {
                    remove(watch);
                }
            }
        }
    };

    private static class PathWatch {

        final WatchKey key;
        final Path path;
        final List<Path> subPaths;
        final List<PathListener> listeners;

        PathWatch(WatchKey key, Path path) {
            this.key = key;
            this.path = path;
            this.subPaths = new ArrayList<>();
            this.listeners = new ArrayList<>();
        }

        synchronized void addListener(Path subPath, PathListener listener) {
            subPaths.add(subPath);
            listeners.add(listener);
        }

        synchronized void removeListener(Path subPath, PathListener listener) {
            for(int i = listeners.size(); i-- > 0;){
                if(subPaths.get(i).equals(subPath) && listeners.get(i).equals(listener)){
                    listeners.remove(i);
                    subPaths.remove(i);  
                    break;
                }
            }
        }
    }

    public interface PathListener {

        void onUpdate(Path path);

        void onDelete(Path path);
    }
}
