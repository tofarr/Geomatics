
package org.om.store.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 *
 * @author tofar
 */
public class RandomAccessFileOutputStream extends OutputStream{
    
    private final RandomAccessFile file;
    private boolean close;

    public RandomAccessFileOutputStream(RandomAccessFile file, boolean close) {
        this.file = file;
        this.close = close;
    }

    @Override
    public void write(int b) throws IOException {
        file.write(b);
    }

    @Override
    public void close() throws IOException {
        if(close){
            file.close();
            close = false;
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        file.write(b, off, len); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void write(byte[] b) throws IOException {
        file.write(b); //To change body of generated methods, choose Tools | Templates.
    }
    
}
