package org.roa;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.roa.attr.AttrSet;
import org.roa.criteria.Criteria;
import org.rdb.trigger.TriggerContext;

/**
 *
 * @author tofarr
 */
public class StoreInfo {

    public final String name;
    public final StoreType type;
    /** if wrapped, must be equal or subset of wrapped attrs. Will not exist for free form or wrapped is free form */
    public final AttrSet attrs;
    /** criteria applied to all elements in this set (Useful when wrapping) */
    public final Criteria criteria;
    /** Sometimes a store is actually a wrapper for another store */
    public final StorePath wrapped;
    /** If wrapped, must be a subset of wrapped attrs */ 
    public final Entitlements entitlements;
    /** If true, each entry has its own entitlements. If false, copied from store */
    public final boolean entryEntitlements;
    public final CacheMode cacheMode;
    private final long cacheTimeout;

    @ConstructorProperties({"name", "type", "attrs", "criteria", "wrapped", "entitlements", "entryEntitlements", "cacheMode", "cacheTimeout"})
    public StoreInfo(String name, StoreType type, AttrSet attrs, Criteria criteria, StorePath wrapped, Entitlements entitlements, boolean entryEntitlements, CacheMode cacheMode, long cacheTimeout) {
        this.name = name;
        this.type = type;
        this.attrs = attrs;
        this.criteria = criteria;
        this.wrapped = wrapped;
        this.entitlements = entitlements;
        this.entryEntitlements = entryEntitlements;
        this.cacheMode = cacheMode;
        this.cacheTimeout = cacheTimeout;
    }

}
