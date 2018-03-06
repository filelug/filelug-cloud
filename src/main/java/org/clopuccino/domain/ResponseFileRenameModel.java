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
public class ResponseFileRenameModel extends ResponseModel {

    @JsonProperty("result")
    private FileRenameModel result;

    public ResponseFileRenameModel() {
        super();
    }

    public FileRenameModel getResult() {
        return result;
    }

    public void setResult(FileRenameModel result) {
        this.result = result;
    }
}
