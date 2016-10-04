package org.rdb.security;

import java.util.Set;

/**
 *
 * @author tofarr
 */
public interface SecurityContext {

    Set<String> getGroupIds();
}
