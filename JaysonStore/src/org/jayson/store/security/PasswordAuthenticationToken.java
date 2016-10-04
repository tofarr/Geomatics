
package org.jayson.store.security;

import java.beans.ConstructorProperties;

/**
 *
 * @author tofarr
 */
public class PasswordAuthenticationToken extends AuthenticationToken {
    
    public final String password;

    @ConstructorProperties({"userId", "password"})
    public PasswordAuthenticationToken(String userId, String password) {
        super(userId);
        this.password = password;
    }
    
    
}
