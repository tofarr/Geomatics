package org.roa;

/**
 *
 * @author tofarr
 */
public enum LockType {
    
    READ,
    WRITE,
    READ_WRITE;
    
    public boolean preventRead(){
        return this != READ;
    }
}
