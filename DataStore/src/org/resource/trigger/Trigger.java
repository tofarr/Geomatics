package org.resource.trigger;

import org.resource.Entry;

//WHAT IS THE ACCESS LEVEL FOR TRIGGERS?

//LESS USEFUL IF SAME AS EXTERNAL.

//NEED UNIQUE RULES FOR THIS - TRIGGERS FROM A PARTICULAR REPOSITORY MAY HAVE A DIFFERENT
//ACCESS LEVEL TO TRIGGERS FROM ANOTHER REPOSITORY!

//should a trigger inherit the access level of the person that wrote the trigger or the
//person running the query?

triggers should have a different access level.

should the access level for a trigger be defined in the trigger?

how do we define this?

/**
 *
 * @author tofarr
 */
public interface Trigger<E> {

    public static final int BEFORE_ADD = 1;
    public static final int BEFORE_GET = 2;
    public static final int BEFORE_UPDATE = 4;
    public static final int BEFORE_REMOVE = 8;
    public static final int AFTER_ADD = 16;
    public static final int AFTER_GET = 32;
    public static final int AFTER_UPDATE = 64;
    public static final int AFTER_REMOVE = 128;

    int getEvent();

    void exec(int operation, Entry<E> entry, Application application);
}
