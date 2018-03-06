package org.clopuccino.domain;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.CountDownLatch;

/**
 * <code>CloseLatchAndDownloadResponse</code> wraps a <code>HttpServletResponse</code> and <code>CountDownLatch</code>.
 *
 * @author masonhsieh
 * @version 1.0
 */
public class CloseLatchAndDownloadResponse {

    private HttpServletResponse response;

    private CountDownLatch closeLatch;

    public CloseLatchAndDownloadResponse() {}

    public CloseLatchAndDownloadResponse(CountDownLatch closeLatch, HttpServletResponse response) {
        this.closeLatch = closeLatch;
        this.response = response;
    }

    public CountDownLatch getCloseLatch() {
        return closeLatch;
    }

    public void setCloseLatch(CountDownLatch closeLatch) {
        this.closeLatch = closeLatch;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }
}
