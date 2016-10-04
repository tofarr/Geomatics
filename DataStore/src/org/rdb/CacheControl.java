
package org.rdb;

/**
 *
 * @author tofarr
 */
public class CacheControl {
    
    public final CacheMode mode;
    public final long time;
    
    public CacheControl(CacheMode mode, long time){
        this.mode = mode;
        this.time = time;
    }
}
