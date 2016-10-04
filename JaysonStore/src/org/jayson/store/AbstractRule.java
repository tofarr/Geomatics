package org.jayson.store;

/**
 *
 * @author tofarr
 */
public abstract class AbstractRule implements Comparable<AbstractRule> {

    public final long id;
    public final int priority;
    public final Path path;

    public AbstractRule(long id, int priority, Path path) {
        this.id = id;
        this.priority = priority;
        this.path = path;
    }

    @Override
    public int compareTo(AbstractRule o) {
        return priority - o.priority;
    }

}
