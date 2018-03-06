package org.clopuccino.domain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.CountDownLatch;

/**
 * <code>CloseLatchAndUploadRequest</code> wraps a <code>HttpServletRequest</code>, <code>HttpServletResponse</code> and <code>CountDownLatch</code>.
 *
 * @author masonhsieh
 * @version 1.0
 */
public class CloseLatchAndUploadRequest {

    private CountDownLatch closeLatch;

    private HttpServletRequest request;

    private HttpServletResponse response;

    public CloseLatchAndUploadRequest() {}

    public CloseLatchAndUploadRequest(CountDownLatch closeLatch, HttpServletRequest request, HttpServletResponse response) {
        this.closeLatch = closeLatch;
        this.request = request;
        this.response = response;
    }

    public CountDownLatch getCloseLatch() {
        return closeLatch;
    }

    public void setCloseLatch(CountDownLatch closeLatch) {
        this.closeLatch = closeLatch;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }
}
