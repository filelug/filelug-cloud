package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * <code></code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseRootDirectoryArrayModel extends ResponseModel {

    @JsonProperty("rootDirectories")
    private List<RootDirectory> rootDirectories;

    public ResponseRootDirectoryArrayModel() {
        super();
    }

    public List<RootDirectory> getRootDirectories() {
        return rootDirectories;
    }

    public void setRootDirectories(List<RootDirectory> rootDirectories) {
        this.rootDirectories = rootDirectories;
    }

    public void addRootDirectory(RootDirectory rootDirectory) {
        if (rootDirectories == null) {
            rootDirectories = new ArrayList<>();
        }

        rootDirectories.add(rootDirectory);
    }

    public void removeRootDirectory(RootDirectory rootDirectory) {
        if (rootDirectories != null && rootDirectories.size() > 0) {
            rootDirectories.remove(rootDirectory);
        }
    }
}
