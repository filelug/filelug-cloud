package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>RequestChangeComputerNameModel</code> represents the request information to change the name of the computer for websocket.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestChangeComputerNameModel extends RequestModel {

    @JsonProperty("new-computer-group")
    private String newComputerGroup;

    @JsonProperty("new-computer-name")
    private String newComputerName;

    public RequestChangeComputerNameModel() {
        super();
    }

    public RequestChangeComputerNameModel(Integer sid, String operatorId, String locale, String newComputerGroup, String newComputerName) {
        super(sid, operatorId, locale);

        this.newComputerGroup = newComputerGroup;

        this.newComputerName = newComputerName;
    }

    public String getNewComputerGroup() {
        return newComputerGroup;
    }

    public void setNewComputerGroup(String newComputerGroup) {
        this.newComputerGroup = newComputerGroup;
    }

    public String getNewComputerName() {
        return newComputerName;
    }

    public void setNewComputerName(String newComputerName) {
        this.newComputerName = newComputerName;
    }
}
