package org.om.stream;

import java.beans.ConstructorProperties;
import java.beans.Transient;
import java.io.File;
import java.io.Serializable;
import org.om.element.BoolElement;
import org.om.element.NumElement;
import org.om.element.StrElement;
import org.om.element.ValElement;
import org.om.store.Capabilities;
import org.om.store.StoreException;
import org.om.store.key.KeyGenerator;

/**
 *
 * @author tofar
 */
public class DirStreamStore implements StreamStore, Serializable {

    private final File dir;
    private final KeyGenerator generator;

    @ConstructorProperties({"dirName", "generator"})
    public DirStreamStore(String dirName, KeyGenerator generator) {
        if (dirName == null) {
            throw new NullPointerException("dirName must not be null");
        }
        this.dir = new File(dirName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Not a directory : " + dirName);
        }
        this.generator = generator;
    }

    @Transient
    public File getDir() {
        return dir;
    }

    public String getDirName() {
        return dir.getPath();
    }

    public KeyGenerator getGenerator() {
        return generator;
    }

    @Override
    public Capabilities getCapabilities() {
        if (dir.canRead()) {
            return dir.canWrite() ? Capabilities.ALL : Capabilities.READ_ONLY;
        } else if (dir.canWrite()) {
            return new Capabilities(Capabilities.CREATE);
        } else {
            return Capabilities.NONE;
        }
    }

    @Override
    public StreamHandle get(ValElement key) throws StoreException {
        File file = toFile(key);
        return (file == null) ? null : new FileStreamHandle(key, file);
    }

    @Override
    public StreamHandle create() throws StoreException {
        ValElement key = generator.createKey();
        return new FileStreamHandle(key, toFile(key));
    }

    @Override
    public boolean load(StreamHandleProcessor processor) throws StoreException {
        for(File file : dir.listFiles()){
            ValElement key = toKey(file);
            if(!processor.process(new FileStreamHandle(key, dir))){
                return false;
            }
        }
        return true;
    }

    @Override
    public long count() throws StoreException {
        return dir.list().length;
    }

    @Override
    public boolean remove(ValElement key) throws StoreException {
        File file = toFile(key);
        return file.delete();
    }

    protected File toFile(ValElement key) {
        String str = key.getVal().toString();
        if(str.contains(File.separator)){
            throw new StoreException("Invalid key : "+key);
        }
        return new File(dir, str);
    }

    protected ValElement toKey(File file) {
        switch(generator.getType()){
            case NUMBER:
                return NumElement.valueOf(Double.parseDouble(file.getName()));
            case STRING:
                return StrElement.valueOf(file.getName());
            case BOOLEAN:
                return BoolElement.valueOf(Boolean.parseBoolean(file.getName()));
            default:
                throw new StoreException("Could not create key for type " + generator.getType());
        }
    }

}
