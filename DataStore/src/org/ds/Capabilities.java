package org.ds;

import java.beans.ConstructorProperties;

/**
 *
 * @author tofarr
 */
public class Capabilities {

    public static final int CREATE = 1;
    public static final int READ = 2;
    public static final int UPDATE = 4;
    public static final int REMOVE = 8;
    public static final int COUNT = 16;
    public static final Capabilities ALL = new Capabilities(CREATE | READ | UPDATE | REMOVE | COUNT);
    public static final Capabilities READ_ONLY = new Capabilities(READ | COUNT);
    public static final Capabilities NO_CHANGES = new Capabilities(CREATE | READ | COUNT);
    public static final Capabilities NONE = new Capabilities(0);

    private final int capabilities;

    @ConstructorProperties({"capabilities"})
    public Capabilities(int capabilities) {
        this.capabilities = capabilities;
    }

    public int getCapabilities() {
        return capabilities;
    }

    public boolean canCreate() {
        return (capabilities & CREATE) != 0;
    }

    public boolean canRead() {
        return (capabilities & READ) != 0;
    }

    public boolean canUpdate() {
        return (capabilities & UPDATE) != 0;
    }

    public boolean canRemove() {
        return (capabilities & REMOVE) != 0;
    }

    public boolean containsAll(Capabilities capabilities) {
        return (this.capabilities & capabilities.capabilities) == capabilities.capabilities;
    }

    public boolean canCount() {
        return (capabilities * COUNT) != 0;
    }

    public Capabilities intersection(Capabilities capabilities) {
        int c = this.capabilities & capabilities.capabilities;
        if (c == this.capabilities) {
            return this;
        } else if (c == capabilities.capabilities) {
            return capabilities;
        } else {
            return new Capabilities(c);
        }
    }

}
