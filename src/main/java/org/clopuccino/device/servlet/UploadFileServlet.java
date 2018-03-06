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
@WebServlet(name = "upload-file-from-device", displayName = "upload-file-from-device", description = "Upload file from device", urlPatterns = {"/directory/dupload"}, asyncSupported = true)
public class UploadFileServlet extends AbstractUploadFileServlet {

    private static final long serialVersionUID = -8808601826895864433L;

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(UploadFileServlet.class.getSimpleName());


    @Override
    public Integer getDesktopSid() {
        return Sid.UPLOAD_FILE;
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    public UploadFileServlet() {
        super();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }
}
