package org.clopuccino.domain;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.clopuccino.Constants;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.ResourceBundle;

/**
 * <code>HierarchicalModel</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HierarchicalModel {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(HierarchicalModel.class.getSimpleName());

//    public static final SimpleDateFormat FILE_DATE_FORMAT = new SimpleDateFormat(Constants.DATE_FORMAT);

    private static final String CONTENT_TYPE_UNKNOWN = "application/octet-stream";

    private static final ResourceBundle mimeTypeBundle = ResourceBundle.getBundle("MimeType");

    @JsonProperty("symlink")
    protected Boolean symlink;

    @JsonProperty("name")
    protected String name;

    @JsonProperty("parent")
    protected String parent;

    @JsonProperty("realName")
    protected String realName;

    @JsonProperty("realParent")
    protected String realParent;

    @JsonProperty("readable")
    protected Boolean readable;

    /* owner only */
    @JsonProperty("writable")
    protected Boolean writable;

    /* owner only */
    @JsonProperty("executable")
    protected Boolean executable;

    /* The size of a directory recursively (sum of the length of all files), calculated on demand */
    @JsonProperty("displaySize")
    protected String displaySize;

    @JsonProperty("sizeInBytes")
    protected Long sizeInBytes;

    @JsonProperty("hidden")
    protected Boolean hidden;

    @JsonProperty("lastModified")
    protected String lastModified;

    /* default use name() as the json value, such as FILE and DIRECTORY */
    @JsonProperty("type")
    protected HierarchicalModelType type;

    @JsonProperty("contentType")
    protected String contentType;

    public File toFile() throws Exception {
        File newFile = new File(parent, name);

        newFile.setExecutable(executable);
        newFile.setWritable(writable);
        newFile.setReadable(readable);
        newFile.setLastModified(Constants.DEFAULT_DATE_FORMAT.parse(lastModified).getTime());

        return newFile;
    }

    public static String prepareContentType(File file) {
        String mimeType;

        String fileExtension = FilenameUtils.getExtension(file.getName());

        if (fileExtension != null && fileExtension.trim().length() > 0 && mimeTypeBundle.containsKey(fileExtension)) {
            mimeType = mimeTypeBundle.getString(fileExtension);
        } else {
            mimeType = CONTENT_TYPE_UNKNOWN;
        }

        return mimeType;
    } // end prepareContenttype()

    public static String prepareContentTypeUsingTika(File file) {
        String mimeType = null;
        FileInputStream fileInputStream = null;

        try {
            fileInputStream = new FileInputStream(file);

            BodyContentHandler contentHandler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            metadata.set(Metadata.RESOURCE_NAME_KEY, file.getName());

            AutoDetectParser parser = new AutoDetectParser();
            parser.parse(fileInputStream, contentHandler, metadata);

            mimeType = metadata.get(Metadata.CONTENT_TYPE);
        } catch (Throwable t) {
            LOGGER.warn("Failed to find content type of file: " + file.getAbsolutePath(), t);
            mimeType = CONTENT_TYPE_UNKNOWN;
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (Throwable t) {
                    /* ignored */
                }
            }
        }

        return mimeType;
    } // end prepareContentTypeUsingTika()

    /**
     * Subclasses must implement this method.
     * @return
     * @param sizeInBytes
     */
    public String calcuateDisplaySize(long sizeInBytes) {
        return "";
    }

    public Boolean getExecutable() {
        return executable;
    }

    public void setExecutable(Boolean executable) {
        this.executable = executable;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getRealParent() {
        return realParent;
    }

    public void setRealParent(String realParent) {
        this.realParent = realParent;
    }

    public Boolean getReadable() {
        return readable;
    }

    public void setReadable(Boolean readable) {
        this.readable = readable;
    }

    public Boolean getSymlink() {
        return symlink;
    }

    public void setSymlink(Boolean symlink) {
        this.symlink = symlink;
    }

    public Boolean getWritable() {
        return writable;
    }

    public void setWritable(Boolean writable) {
        this.writable = writable;
    }

    public String getDisplaySize() {
        return displaySize;
    }

    public void setDisplaySize(String displaySize) {
        this.displaySize = displaySize;
    }

    public Long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(Long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

    public HierarchicalModelType getType() {
        return type;
    }

    public void setType(HierarchicalModelType type) {
        this.type = type;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HierarchicalModel{");
        sb.append("contentType='").append(contentType).append('\'');
        sb.append(", symlink=").append(symlink);
        sb.append(", name='").append(name).append('\'');
        sb.append(", parent='").append(parent).append('\'');
        sb.append(", realName='").append(realName).append('\'');
        sb.append(", realParent='").append(realParent).append('\'');
        sb.append(", readable=").append(readable);
        sb.append(", writable=").append(writable);
        sb.append(", executable=").append(executable);
        sb.append(", displaySize='").append(displaySize).append('\'');
        sb.append(", sizeInBytes=").append(sizeInBytes);
        sb.append(", hidden=").append(hidden);
        sb.append(", lastModified='").append(lastModified).append('\'');
        sb.append(", type=").append(type);
        sb.append('}');
        return sb.toString();
    }
}
