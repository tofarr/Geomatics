package org.om.store.jdbc;

import java.io.IOException;
import java.io.Reader;

/**
 *
 * @author tofar
 */
public class StringBuilderReader extends Reader {

    private final StringBuilder str;
    private int index;

    public StringBuilderReader(StringBuilder str) {
        this.str = str;
    }

    public StringBuilder getStr() {
        return str;
    }

    @Override
    public void reset() {
        index = 0;
    }

    @Override
    public long skip(long n) throws IOException {
        int ret = (int) Math.min(str.length() - index, n);
        index += ret;
        return ret;
    }

    @Override
    public int read() throws IOException {
        return super.read(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int ret = Math.min(str.length() - index, len);
        str.getChars(index, index + ret, cbuf, off);
        index += ret;
        return ret;
    }

    @Override
    public void close() {
    }
}
