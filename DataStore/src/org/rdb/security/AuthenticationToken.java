
package org.rdb.security;

/**
 *
 * @author tofarr
 */
public abstract class AuthenticationToken {
    
    public final String userId;

    public AuthenticationToken(String userId) {
        this.userId = userId;
    }
}
