package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * <code>ResponseListRootsModel</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseListRootsModel extends ResponseModel {

    @JsonProperty("roots")
    private List<RootDirectory> roots;

    public ResponseListRootsModel() {
        super();
    }

    public List<RootDirectory> getRoots() {
        return roots;
    }

    public void setRoots(List<RootDirectory> roots) {
        this.roots = roots;
    }

    public void addRoot(RootDirectory root) {
        if (roots == null) {
            roots = new ArrayList<>();
        }

        roots.add(root);
    }

    public void removeRoot(RootDirectory root) {
        if (roots != null && roots.size() > 0) {
            roots.remove(root);
        }
    }
}
