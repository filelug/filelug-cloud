package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.FileUploadDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.DatabaseConstants;
import org.clopuccino.domain.ConfirmTransferModel;
import org.clopuccino.domain.FileUpload;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>ConfirmUploadFileServlet2</code> supports multiple query for status of file uploads.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "confirm-upload-file2", displayName = "confirm-upload-file2", description = "Query the result of files upload", urlPatterns = {"/directory/dcupload2"})
public class ConfirmUploadFileServlet2 extends HttpServlet {
    
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ConfirmUploadFileServlet2.class.getSimpleName());

    private static final long serialVersionUID = 8950605777507175293L;

    private final FileUploadDao fileUploadDao;

    public ConfirmUploadFileServlet2() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        fileUploadDao = new FileUploadDao(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ObjectMapper mapper = Utility.createObjectMapper();

            List<ConfirmTransferModel> uploadModels = mapper.readValue(new InputStreamReader(req.getInputStream(), "UTF-8"), new TypeReference<List<ConfirmTransferModel>>() {
            });

            if (uploadModels != null && uploadModels.size() > 0) {
                List<String> transferKeys = new ArrayList<>();

                for (ConfirmTransferModel confirmTransferModel : uploadModels) {
                    String transferKey = confirmTransferModel.getTransferKey();

                    if (transferKey != null && transferKey.trim().length() > 0) {
                        transferKeys.add(transferKey);
                    }
                }

                List<ConfirmTransferModel> foundTransferModels = fileUploadDao.findFileUploadStatusForUploadKeys(transferKeys);


                if (foundTransferModels == null) {
                    // Not found, return status of not_found

                    foundTransferModels = new ArrayList<>();

                    for (String transferKey : transferKeys) {
                        ConfirmTransferModel confirmTransferModel = new ConfirmTransferModel(transferKey, DatabaseConstants.TRANSFER_STATUS_NOT_FOUND, null);
                        foundTransferModels.add(confirmTransferModel);
                    }

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write(mapper.writeValueAsString(foundTransferModels));
                    resp.getWriter().flush();
                } else {
                    for (ConfirmTransferModel confirmTransferModel : foundTransferModels) {
                        String uploadStatus = confirmTransferModel.getStatus();

                        String transferKey = confirmTransferModel.getTransferKey();

                        if (transferKey != null && transferKey.trim().length() > 0) {
                            final FileUpload fileUpload = fileUploadDao.findFileUploadForUploadKey(transferKey);

                            if (fileUpload != null) {
                                // delete tmp file if exists, under one of the following conditions meet:
                                // (1) if the upload status is success
                                // (2) if the request from device is version 2, instead of version 3.

                                Long lastModifiedTimestamp = fileUpload.getSourceFileLastModifiedTimestamp();

                                if (uploadStatus.equals(DatabaseConstants.TRANSFER_STATUS_SUCCESS) || lastModifiedTimestamp == null || lastModifiedTimestamp == 0L) {
                                    String tmpFileAbsolutePath = fileUpload.getTmpFile();

                                    Utility.deleteUploadTmpFileAndUpdateRecord(tmpFileAbsolutePath, fileUploadDao, transferKey);
                                }
                            }
                        }
                    }

                    if (foundTransferModels.size() < transferKeys.size()) {
                        // Get all transfer keys in found models

                        List<String> foundTransferKeysInDb = new ArrayList<>();

                        for (ConfirmTransferModel confirmTransferModel : foundTransferModels) {
                            foundTransferKeysInDb.add(confirmTransferModel.getTransferKey());
                        }

                        // Set not_found to the transfer keys that are not found in db

                        for (String transferKey : transferKeys) {
                            if (!foundTransferKeysInDb.contains(transferKey)) {
                                ConfirmTransferModel confirmTransferModel = new ConfirmTransferModel(transferKey, DatabaseConstants.TRANSFER_STATUS_NOT_FOUND, null);
                                foundTransferModels.add(confirmTransferModel);
                            }
                        }
                    }

                    resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write(mapper.writeValueAsString(foundTransferModels));
                    resp.getWriter().flush();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on confirming upload file(V2).", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
