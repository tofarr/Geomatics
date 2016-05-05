package org.jayson;

/**
 *
 * @author tofarrell
 */
public abstract class AbstractPrioritized implements Comparable<AbstractPrioritized> {

    public static final int EARLY = 10000;
    
    public static final int MED = 20000;
    
    public static final int LATE = 30000;
    
    public final int priority;

    public AbstractPrioritized(int priority) {
        this.priority = priority;
    }

    @Override
    public int compareTo(AbstractPrioritized other) throws NullPointerException {
        return priority - other.priority;
    }
}
