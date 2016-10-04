package org.jayson.store;

import java.beans.ConstructorProperties;
import org.jayson.store.security.AuthorizationToken;

/**
 *
 * @author tofarr
 */
public class EditContext {

    public final AuthorizationToken token;
    public final Edit edit;

    @ConstructorProperties({"token", "edit"})
    public EditContext(AuthorizationToken token, Edit edit) {
        this.token = token;
        this.edit = edit;
    }

}
