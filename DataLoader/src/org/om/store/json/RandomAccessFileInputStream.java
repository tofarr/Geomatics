/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.om.store.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 *
 * @author tofar
 */
public class RandomAccessFileInputStream extends InputStream {
    
    private final RandomAccessFile file;

    public RandomAccessFileInputStream(RandomAccessFile file) {
        this.file = file;
    }

    @Override
    public int read() throws IOException {
        return file.read();
    }

    @Override
    public int available() throws IOException {
        long ret = file.length() - file.getFilePointer();
        return (ret > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int)ret;
    }

    @Override
    public long skip(long n) throws IOException {
        long offset = file.getFilePointer();
        long len = file.length();
        n = Math.min(n, len - offset);
        file.seek(offset + n);
        return n;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return file.read(b, off, len); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int read(byte[] b) throws IOException {
        return file.read(b); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() throws IOException {
        file.close();
    }
    
    
}
