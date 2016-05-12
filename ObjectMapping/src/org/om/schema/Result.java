package org.om.schema;

import java.beans.ConstructorProperties;

/**
 *
 * @author tofarrell
 */
public class Result {

    private static final String[] NO_PARAMS = new String[0];
    public static final Result[] NO_CHILDREN = new Result[0];
    private final Path path;
    private final boolean success;
    private final String code;
    private final String[] params;
    private final Result[] children;

    @ConstructorProperties({"path", "success", "code", "params", "children"})
    public Result(Path path, boolean success, String code, String[] params, Result[] children) {
        this.path = path;
        this.success = success;
        this.code = code;
        this.params = (params == null) ? NO_PARAMS : params.clone();
        this.children = (children == null) ? NO_CHILDREN : children.clone();
    }

    public Path getPath() {
        return path;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getCode() {
        return code;
    }

    public String[] getParams() {
        return (params.length == 0) ? params : params.clone();
    }

    public int numParams() {
        return params.length;
    }

    public String paramAt(int index) {
        return params[index];
    }

    public Result[] getChildren() {
        return (children.length == 0) ? children : children.clone();
    }

    public int numChildren() {
        return children.length;
    }

    public Result childAt(int index) {
        return children[index];
    }
}
