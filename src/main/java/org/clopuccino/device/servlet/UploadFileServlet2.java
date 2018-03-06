package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import org.clopuccino.server.servlet.Sid;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <code>UploadFileServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "upload-file-from-device2", asyncSupported = true, displayName = "upload-file-from-device2", description = "Upload file from device2", urlPatterns = {"/directory/dupload2"})
public class UploadFileServlet2 extends AbstractUploadFileServlet {

    private static final long serialVersionUID = 6284648647899784203L;

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(UploadFileServlet2.class.getSimpleName());

    @Override
    public Integer getDesktopSid() {
        return Sid.UPLOAD_FILE2;
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    public UploadFileServlet2() {
        super();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }
}
