package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code></code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseFindFileByPathModel extends ResponseModel {

    @JsonProperty("result")
    private HierarchicalModel result;

    public ResponseFindFileByPathModel() {
        super();
    }

    public HierarchicalModel getResult() {
        return result;
    }

    public void setResult(HierarchicalModel result) {
        this.result = result;
    }
}
