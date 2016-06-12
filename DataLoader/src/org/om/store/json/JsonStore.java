package org.om.store.json;

import java.beans.ConstructorProperties;
import java.beans.Transient;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import org.jayson.Jayson;
import org.jayson.JaysonException;
import org.jayson.JaysonReader;
import org.jayson.JaysonType;
import org.jayson.JaysonWriter;
import org.jayson.PrettyPrintJaysonWriter;
import org.om.attr.AttrSet;
import org.om.criteria.All;
import org.om.criteria.Criteria;
import org.om.element.Element;
import org.om.element.ObjElement;
import org.om.sort.Sorter;
import org.om.store.Capabilities;
import org.om.store.ElementProcessor;
import org.om.store.ElementStore;
import org.om.store.StoreException;

/**
 * Store consisting of a file containing a UTF-8 encoded JSON array. Updates /
 * Deletes cause entries in the file to be replaced with white space, and new
 * entries to be written to the end of the file. File can also be compressed to
 * save space.
 *
 * Include generated attribute index, the byte offset within the file?
 *
 * @author tofar
 */
public class JsonStore implements ElementStore {

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private final File file;
    private final AttrSet attrs; // should include offset attr
    private final boolean allowUnknowns;
    private final boolean prettyPrint;

    //File stats
    private long createOffset;
    private long count;

    @ConstructorProperties({"fileName", "attrs", "allowUnknowns", "prettyPrint"})
    public JsonStore(String fileName, AttrSet attrs, boolean allowUnknowns, boolean prettyPrint) {
        this.file = new File(fileName);
        this.attrs = attrs;
        this.allowUnknowns = allowUnknowns;
        this.prettyPrint = prettyPrint;
        createOffset = -1;
    }

    public static JsonStore valueOf(File metaFile) {
        JsonStore store;
        try (Reader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(metaFile)), UTF8)) {
            Jayson jayson = Jayson.getInstance();
            store = jayson.parse(JsonStore.class, reader);
        } catch (IOException ex) {
            throw new StoreException("Could not read meta : " + metaFile, ex);
        }
        store.readFileStats();
        return store;
    }

    @Transient
    @Override
    public Capabilities getCapabilities() {
        File f = file;
        if (!f.exists()) {
            f = f.getParentFile();
        }
        int ret = Capabilities.READ_ONLY.getCapabilities();
        if (f.canWrite()) {
            ret |= Capabilities.CREATE | Capabilities.REMOVE | Capabilities.UPDATE;
        }
        return new Capabilities(ret);
    }

    @Override
    public AttrSet getAttrs() {
        return attrs;
    }

    public boolean isAllowUnknowns() {
        return allowUnknowns;
    }

    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    public String getFileName() {
        return file.toString();
    }

    @Override
    public boolean load(List<String> attrNames, Criteria criteria, Sorter sorter, ElementProcessor processor) throws StoreException {
        if (!file.exists()) {
            return true; // nothing to load!
        }
        AttrSet attrsToLoad = (attrNames == null) ? this.attrs : this.attrs.filter(attrNames);
        try (JaysonReader reader = new JaysonReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), UTF8))) {
            reader.next(JaysonType.BEGIN_ARRAY);
            JaysonType type = reader.next();
            while (type != JaysonType.END_ARRAY) {
                ObjElement element = (ObjElement) Element.readJson(type, reader);
                if (criteria.match(element)) {
                    element = attrsToLoad.filterElement(element);
                    if (!processor.process(element)) {
                        return false;
                    }
                }
            }
            return true;
        } catch (IOException ex) {
            throw new StoreException(ex);
        }
    }

    @Override
    public long count(Criteria criteria) throws StoreException {
        if (!file.exists()) {
            return 0; // nothing to load!
        }
        if ((criteria == null) || (criteria == All.INSTANCE)) {
            return count;
        }
        try (JaysonReader reader = new JaysonReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), UTF8))) {
            int ret = 0;
            reader.next(JaysonType.BEGIN_ARRAY);
            JaysonType type = reader.next();
            while (type != JaysonType.END_ARRAY) {
                ObjElement element = (ObjElement) Element.readJson(type, reader);
                if (criteria.match(element)) {
                    ret++;
                }
            }
            return ret;
        } catch (IOException ex) {
            throw new StoreException(ex);
        }
    }

    @Override
    public ObjElement create(ObjElement element) throws StoreException {
        attrs.validate(element, allowUnknowns);
        if (createOffset < 0) {
            createFile();
        }
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.seek(createOffset);
            try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new RandomAccessFileOutputStream(raf, false)), UTF8)) {
                if (count > 1) {
                    writer.write(',');
                }
                element.toStream(prettyPrint, writer);
                count++;
                writer.write(']');
            }
            createOffset = raf.getFilePointer() - 1;
            return element;
        } catch (IOException ex) {
            throw new StoreException(ex);
        }
    }

    @Override
    public long update(Criteria criteria, ObjElement updates) throws StoreException {
        if (!file.exists()) {
            return 0; // nothing to load!
        }
        try {
            File tmp = File.createTempFile("JsonStore", ".json");
            try {
                long ret = 0;
                try (JaysonReader reader = new JaysonReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), UTF8));
                        JaysonWriter writer = new JaysonWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(tmp)), UTF8))) {
                    reader.next(JaysonType.BEGIN_ARRAY);
                    writer.beginArray();
                    JaysonType type = reader.next();
                    while (type != JaysonType.END_ARRAY) {
                        ObjElement element = (ObjElement) Element.readJson(type, reader);
                        if (criteria.match(element)) {
                            element = element.merge(updates);
                            attrs.validate(element, allowUnknowns);
                            ret++;
                        }
                        element.toJson(writer);
                    }
                    writer.endArray();
                }
                Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return ret;
            } finally {
                Files.delete(tmp.toPath());
                tmp.delete();
            }
        } catch (IOException ex) {
            throw new StoreException(ex);
        }
    }

    @Override
    public long remove(Criteria criteria) throws StoreException {
        if (!file.exists()) {
            return 0; // nothing to load!
        }
        try {
            File tmp = File.createTempFile("JsonStore", ".json");
            try {
                try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                    CountInputStream input = new CountInputStream(new BufferedInputStream(new RandomAccessFileInputStream(raf)));
                    JaysonReader reader = new JaysonReader(new InputStreamReader(input, UTF8));
                    reader.next(JaysonType.BEGIN_ARRAY);
                    long prevOffset = input.getCount();
                    JaysonType type = reader.next();
                    int ret = 0;
                    while (type != JaysonType.END_ARRAY) {
                        ObjElement element = (ObjElement) Element.readJson(type, reader);
                        long offset = input.getCount();
                        if (criteria.match(element)) {
                            raf.seek(prevOffset);
                            byte[] buffer = new byte[(int) (offset - prevOffset)];
                            Arrays.fill(buffer, (byte) 32); //space
                            raf.write(buffer);
                            ret++;
                        }
                        prevOffset = offset;
                    }
                    count -= ret;
                    return ret;
                }
            } finally {
                Files.delete(tmp.toPath());
                tmp.delete();
            }
        } catch (IOException ex) {
            throw new StoreException(ex);
        }
    }

    @Override
    public void createAll(List<ObjElement> elements) throws StoreException {
        for (ObjElement element : elements) {
            attrs.validate(element, allowUnknowns);
        }
        if(createOffset < 0){
            createFile();
        }
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.seek(createOffset);
            try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new RandomAccessFileOutputStream(raf, false)), UTF8)) {
                for (ObjElement element : elements) {
                    if (count > 1) {
                        writer.write(',');
                    }
                    element.toStream(prettyPrint, writer);
                    count++;
                }
                writer.write(']');
            }
            createOffset = raf.getFilePointer() - 1;
        } catch (IOException ex) {
            throw new StoreException(ex);
        }
    }

    public static long formatFile(boolean prettyPrint, File file) throws StoreException {
        try {
            File tmp = File.createTempFile("JsonStore", ".json");
            try {
                long ret = 0;
                try (JaysonReader reader = new JaysonReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), UTF8));
                        JaysonWriter writer = createWriter(prettyPrint, new BufferedOutputStream(new FileOutputStream(tmp)))) {
                    reader.next(JaysonType.BEGIN_ARRAY);
                    writer.beginArray();
                    JaysonType type = reader.next();
                    while (type != JaysonType.END_ARRAY) {
                        ObjElement element = (ObjElement) Element.readJson(type, reader);
                        element.toJson(writer);
                        ret++;
                    }
                    writer.endArray();
                }
                Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return ret;
            } finally {
                tmp.delete();
            }
        } catch (IOException ex) {
            throw new StoreException(ex);
        }
    }

    /**
     * Get the point within the file at which new items should be created.
     * Unfortunately we need to read the whole file forwards in order to
     * accomplish this. (In case of white space, special characters and
     * comments, especially //)
     */
    public void readFileStats() throws StoreException {
        count = 0;
        if (!file.canRead()) {
            if (file.exists()) {
                throw new StoreException("Cannot read file : " + file);
            }
            count = 0;
            createOffset = -1;
            return;
        }
        try (CountInputStream input = new CountInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            try (JaysonReader reader = new JaysonReader(new InputStreamReader(input, UTF8))) {
                reader.next(JaysonType.BEGIN_ARRAY);
                while (true) {
                    JaysonType type = reader.next();
                    switch (type) {
                        case BEGIN_OBJECT:
                            Element element = Element.readJson(type, reader);
                            count++;
                            break;
                        case END_ARRAY:
                            createOffset = input.getCount() - 1;
                            return;
                        default:
                            throw new JaysonException("Unexpected Type : " + type);
                    }
                }
            }
        } catch (IOException ex) {
            throw new StoreException(ex);
        }
    }

    private static JaysonWriter createWriter(boolean prettyPrint, OutputStream out) {
        Writer writer = new OutputStreamWriter(out, UTF8);
        if (prettyPrint) {
            return new PrettyPrintJaysonWriter(writer);
        } else {
            return new JaysonWriter(writer);
        }
    }

    private void createFile() throws StoreException {
        try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)), UTF8)) {
            writer.append("[]");
            createOffset = 1; // [ is 1 byte in UTF8
        } catch (IOException ex) {
            throw new StoreException(ex);
        }
    }
}
