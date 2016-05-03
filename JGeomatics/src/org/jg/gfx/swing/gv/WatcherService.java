package org.jg.gfx.swing.gv;

import java.io.IOException;
import java.nio.file.FileSystems;
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
public class WatcherService {

    private static final Logger LOG = Logger.getLogger(WatcherService.class.getName());

    private WatchService watchService;
    private final ConcurrentHashMap<Path, PathWatch> byPath;
    private final ConcurrentHashMap<WatchKey, PathWatch> byKey;
    private volatile boolean doRun;
    private boolean running;

    public WatcherService() throws IOException {
        byPath = new ConcurrentHashMap<>();
        byKey = new ConcurrentHashMap<>();
    }

    public synchronized void addListener(Path path, PathListener listener) throws IOException {
        if(watchService == null){
            watchService = FileSystems.getDefault().newWatchService();
        }
        PathWatch watch = byPath.get(path);
        if (watch == null) {
            WatchKey key = path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            watch = new PathWatch(key, path);
            byPath.put(path, watch);
            byKey.put(key, watch);
        }
        watch.addListener(listener);
        doRun = true;
        if (!running) {
            new Thread(runnable).start();
        }
    }

    public synchronized void removeListener(Path path, PathListener listener) {
        PathWatch watch = byPath.get(path);
        if (watch == null) {
            return;
        }
        watch.removeListener(listener);
        if (watch.listeners.isEmpty()) {
            byKey.remove(watch.key);
            byPath.remove(watch.path);
        }
        if (byKey.isEmpty()) {
            doRun = false;
        }
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

                listeners.clear();
                synchronized (watch) {
                    listeners.addAll(watch.listeners); // defensive copy!
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    Kind kind = event.kind();
                    if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        for (PathListener listener : listeners) {
                            listener.onUpdate();
                        }
                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        for (PathListener listener : listeners) {
                            listener.onDelete();
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
        final List<PathListener> listeners;

        PathWatch(WatchKey key, Path path) {
            this.key = key;
            this.path = path;
            this.listeners = new ArrayList<>();
        }

        synchronized void addListener(PathListener listener) {
            listeners.add(listener);
        }

        synchronized void removeListener(PathListener listener) {
            listeners.remove(listener);
        }
    }

    public interface PathListener {

        void onUpdate();

        void onDelete();
    }
}
