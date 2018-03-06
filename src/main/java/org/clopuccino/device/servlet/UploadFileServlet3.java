package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <code>UploadFileServlet3</code> supports resumable file uploads.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "upload-file-from-device3", asyncSupported = true, displayName = "upload-file-from-device3", description = "Upload file from device(V3)", urlPatterns = {"/directory/dupload3"})
public class UploadFileServlet3 extends UploadFileServlet4 {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(UploadFileServlet3.class.getSimpleName());

    private static final long serialVersionUID = 2849302840068656223L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        internalDoPost(req, resp, true, LOGGER);
    }
}
