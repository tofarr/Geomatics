package org.roa;

/**
 *
 * @author tofarr
 */
public interface Resource {

    public static final int CAN_ADD = 1;
    public static final int CAN_GET = 2;
    public static final int CAN_UPDATE = 4;
    public static final int CAN_REMOVE = 8;
    public static final int CAN_ITERATE = 16;

    long getLastModified();

    String getETag();

    DO WE HAVE ROW LEVEL ENTITLEMENTS?
    
    IT IS KIND OF USEFUL TO HAVE
    
    //Get set of entitlements for this resource - governing what can be done with it
    Entitlements getEntitlements();
}
