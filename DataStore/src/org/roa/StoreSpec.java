package org.roa;

/**
 *
 * @author tofarr
 */
public class StoreSpec {

    private final String name;
    private final AttrSet attrs; // if wrapped, may be a subset
    private final Criteria criteria;
    private final Triggers triggers;
    private final StorePath wrapped; // present only if the store wraps an existing one, and does not have its own storage
    private final Entitlements entitlements;
    
}
