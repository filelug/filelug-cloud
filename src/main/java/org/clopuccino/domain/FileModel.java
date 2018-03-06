package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.clopuccino.Constants;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * <code>FileModel</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class FileModel extends HierarchicalModel {

    public FileModel() {
        this.type = HierarchicalModelType.FILE;
    }

    public static FileModel createInstance(File file, boolean calculateSize, boolean prepareContentType) throws IOException {
        FileModel model = new FileModel();

        model.setSymlink(FileUtils.isSymlink(file));

        String absPath = FilenameUtils.normalize(file.getAbsolutePath());
        model.setName(FilenameUtils.getName(absPath));
        model.setParent(FilenameUtils.normalizeNoEndSeparator(file.getParentFile().getAbsolutePath()));

        model.setReadable(file.canRead());
        model.setWritable(file.canWrite());
        model.setExecutable(file.canExecute());

        model.setHidden(file.isHidden());

        model.setLastModified(Constants.DEFAULT_DATE_FORMAT.format(new Date(file.lastModified())));

        model.setType(HierarchicalModelType.FILE);

        if (prepareContentType) {
            model.setContentType(prepareContentType(file));
        } else {
            model.setContentType("");
        }

        if (calculateSize) {
            long sizeInBytes = new File(model.parent, model.name).length();

            model.setSizeInBytes(sizeInBytes);
            model.setDisplaySize(model.calcuateDisplaySize(sizeInBytes));
        } else {
            model.setSizeInBytes(0L);
            model.setDisplaySize("");
        }

        return model;
    }

    @Override
    public String calcuateDisplaySize(long sizeInBytes) {
        return FileUtils.byteCountToDisplaySize(sizeInBytes);
    }
}
