package org.lcd;

/**
 *
 * @author tofar
 */
public interface ResultIterator {

    public boolean next();
    
    public AttrSet getAttrs();

    public Object getByName(String name);

    public Object getByIndex(int index);
    
    public long skip(long amt);
}
