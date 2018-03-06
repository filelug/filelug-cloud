package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>RequestFileUploadGroupModel</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestFileUploadGroupModel extends RequestModel {

    @JsonProperty("file-upload-group")
    private FileUploadGroup fileUploadGroup;

    public RequestFileUploadGroupModel(Integer sid, String operatorId, String locale, FileUploadGroup fileUploadGroup) {
        super(sid, operatorId, locale);

        this.fileUploadGroup = fileUploadGroup;
    }

    public FileUploadGroup getFileUploadGroup() {
        return fileUploadGroup;
    }

    public void setFileUploadGroup(FileUploadGroup fileUploadGroup) {
        this.fileUploadGroup = fileUploadGroup;
    }


}
