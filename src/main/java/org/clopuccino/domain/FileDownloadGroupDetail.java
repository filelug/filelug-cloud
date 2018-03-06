package org.clopuccino.domain;


/**
 * <code>FileDownloadGroupDetail</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class FileDownloadGroupDetail {

    private String downloadGroupDetailId;

    private String downloadGroupId;

    private String downloadKey;

    private String filePath;

    public FileDownloadGroupDetail() {
    }

    public FileDownloadGroupDetail(String downloadGroupDetailId,
                                   String downloadGroupId,
                                   String downloadKey,
                                   String filePath) {
        this.downloadGroupDetailId = downloadGroupDetailId;
        this.downloadGroupId = downloadGroupId;
        this.downloadKey = downloadKey;
        this.filePath = filePath;
    }

    public String getDownloadGroupDetailId() {
        return downloadGroupDetailId;
    }

    public void setDownloadGroupDetailId(String downloadGroupDetailId) {
        this.downloadGroupDetailId = downloadGroupDetailId;
    }

    public String getDownloadGroupId() {
        return downloadGroupId;
    }

    public void setDownloadGroupId(String downloadGroupId) {
        this.downloadGroupId = downloadGroupId;
    }

    public String getDownloadKey() {
        return downloadKey;
    }

    public void setDownloadKey(String downloadKey) {
        this.downloadKey = downloadKey;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FileDownloadGroupDetail{");
        sb.append("downloadGroupDetailId='").append(downloadGroupDetailId).append('\'');
        sb.append(", downloadGroupId='").append(downloadGroupId).append('\'');
        sb.append(", downloadKey='").append(downloadKey).append('\'');
        sb.append(", filePath='").append(filePath).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
