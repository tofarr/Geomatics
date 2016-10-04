package org.jayson.store.security;

import java.util.Set;

/**
 *
 * @author tofarr
 */
public interface AuthorizationToken {

    Set<String> getGroupIds();
}
