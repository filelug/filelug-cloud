package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <code>DownloadFileServlet2</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "download-file-to-device2", displayName = "download-file-to-device2", description = "Download file to device (V2)", urlPatterns = {"/directory/ddownload2"})
public class DownloadFileServlet2 extends DownloadFileServlet3 {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DownloadFileServlet2.class.getSimpleName());

    private static final long serialVersionUID = 8785948253812489518L;

    public DownloadFileServlet2() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        internalDoGet(req, resp, true, LOGGER);
    }
}
