package org.clopuccino.domain;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.clopuccino.Constants;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <code>DirectoryModel</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class DirectoryModel extends HierarchicalModel {
    private static final long K = 1024;

    private static final long M = K * K;

    private static final long G = M * K;

    private static final long T = G * K;

    public DirectoryModel() {
        this.type = HierarchicalModelType.DIRECTORY;
    }

    public static DirectoryModel createInstance(File file, boolean calculateSize) throws IOException {
        DirectoryModel model = new DirectoryModel();

        model.setSymlink(FileUtils.isSymlink(file));

        String absPath = FilenameUtils.normalizeNoEndSeparator(file.getAbsolutePath());
        model.setName(FilenameUtils.getName(absPath));
        model.setParent(FilenameUtils.normalizeNoEndSeparator(file.getParentFile().getAbsolutePath()));

        model.setReadable(file.canRead());
        model.setWritable(file.canWrite());
        model.setExecutable(file.canExecute());

        model.setHidden(file.isHidden());

        model.setLastModified(Constants.DEFAULT_DATE_FORMAT.format(new Date(file.lastModified())));

        model.setType(HierarchicalModelType.DIRECTORY);

        model.setContentType("application/directory");

        if (calculateSize) {
            long sizeInBytes = FileUtils.sizeOfDirectory(new File(model.parent, model.name));

            model.setSizeInBytes(sizeInBytes);
            model.setDisplaySize(model.calcuateDisplaySize(sizeInBytes));
        } else {
            model.setSizeInBytes(0L);
            model.setDisplaySize("");
        }

        return model;
    }

    public static String convertToStringRepresentation(final long value) {
        final long[] dividers = new long[]{T, G, M, K, 1};

        final String[] units = new String[]{"TB", "GB", "MB", "KB", "B"};

        if (value < 1) {
            return "";
        }

        String result = null;

        for (int i = 0; i < dividers.length; i++) {
            final long divider = dividers[i];
            if (value >= divider) {
                result = format(value, divider, units[i]);
                break;
            }
        }
        return result;
    }

    private static String format(final long value, final long divider, final String unit) {
        final BigDecimal result = divider > 1 ? BigDecimal.valueOf(value).divide(BigDecimal.valueOf(divider)) : BigDecimal.valueOf(value);
        return String.format("%.2f %s", result.doubleValue(), unit);
    }

//    private static String format(final long value, final long divider, final String unit) {
//        final double result = divider > 1 ? (double) value / (double) divider : (double) value;
//        return String.format("%.2f %s", Double.valueOf(result), unit);
//    }

    @Override
    public String calcuateDisplaySize(long sizeInBytes) {
        return convertToStringRepresentation(sizeInBytes);
    }
}
