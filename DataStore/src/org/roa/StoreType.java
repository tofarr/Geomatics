package org.roa;

/**
 *
 * @author tofarr
 */
public enum StoreType {

    /** Entries must conform to a given schema - additional attributes are ignored */
    SCHEMA_BOUND_OBJECTS,
    /** Entries have no schema - they may contain any combination of attributes */
    FREE_FORM_OBJECTS, // could be done with null attrs.
    /** Entries contain some schema constrained attributes, but also may contain free form data */
    PARTIAL_BOUND_OBJECTS,
    /** Objects contain raw data (such as images), and a mime type */
    DATA
}
