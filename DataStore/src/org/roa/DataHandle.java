package org.roa;

import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author tofarr
 */
public interface DataHandle {
    
    long getSizeInBytes();
    
    InputStream read(SecurityContext context);
    
    OutputStream write(SecurityContext context);
    
    void write(SecurityContext context, InputStream in);
}
