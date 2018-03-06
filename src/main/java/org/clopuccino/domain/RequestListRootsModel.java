package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>RequestListRootsModel</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestListRootsModel extends RequestModel {

    @JsonProperty("show-hidden")
    private boolean showHidden;

    public RequestListRootsModel() {
        super();
    }

    public RequestListRootsModel(Integer sid, String operatorId, String locale, boolean showHidden) {
        super(sid, operatorId, locale);

        this.showHidden = showHidden;
    }

    public boolean isShowHidden() {
        return showHidden;
    }

    public void setShowHidden(boolean showHidden) {
        this.showHidden = showHidden;
    }
}
