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
public class ResponseListDirectoryChildrenModel extends ResponseModel {

    @JsonProperty("children")
    private List<HierarchicalModel> children;

    public ResponseListDirectoryChildrenModel() {
        super();
    }

    public List<HierarchicalModel> getChildren() {
        return children;
    }

    public void setChildren(List<HierarchicalModel> children) {
        this.children = children;
    }

    public void addHierarchicalModel(HierarchicalModel bookmark) {
        if (children == null) {
            children = new ArrayList<>();
        }

        children.add(bookmark);
    }

    public void removeHierarchicalModel(HierarchicalModel bookmark) {
        if (children != null && children.size() > 0) {
            children.remove(bookmark);
        }
    }
}
