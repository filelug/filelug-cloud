package org.clopuccino.service;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A byte range as parsed from a request Range header.  Format produced by this class is
 * also compatible with the X-AppEngine-BlobRange header, used for serving sub-ranges of
 * blobs.
 */
public class HttpFileByteRange {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(HttpFileByteRange.class.getSimpleName());

    private long start;

    private Long end;

    private static final String BYTES_UNIT = "bytes";

    private static final String UNIT_REGEX = "([^=\\s]+)";

    private static final String VALID_RANGE_HEADER_REGEX = UNIT_REGEX + "\\s*=\\s*(\\d*)\\s*-\\s*(\\d*)";

    private static final String INVALID_RANGE_HEADER_REGEX = "((?:\\s*,\\s*(?:\\d*)-(?:\\d*))*)";

    private static final Pattern RANGE_HEADER_PATTERN = Pattern.compile("^\\s*" +
                                                                VALID_RANGE_HEADER_REGEX +
                                                                INVALID_RANGE_HEADER_REGEX +
                                                                "\\s*$");

    private static final String CONTENT_RANGE_UNIT_REGEX = "([^\\s]+)";

    private static final String VALID_CONTENT_RANGE_HEADER_REGEX = BYTES_UNIT + "\\s+(\\d+)-(\\d+)/(\\d+)";

    private static final Pattern CONTENT_RANGE_HEADER_PATTERN = Pattern.compile("^\\s*" + VALID_CONTENT_RANGE_HEADER_REGEX + "\\s*$");

    /**
     * Constructor.
     *
     * @param start Start index of blob range to serve.  If negative, serve the last abs(start) bytes
     *              of the blob.
     */
    public HttpFileByteRange(long start) {
        this(start, null);
    }

    /**
     * Constructor.
     *
     * @param start Start index of blob range to serve.  May not be negative.
     * @param end   End index of blob range to serve.  Index is inclusive, meaning the byte indicated
     *              by end is included in the response.
     */
    public HttpFileByteRange(long start, long end) {
        this(start, Long.valueOf(end));

        if (start < 0) {
            throw new IllegalArgumentException("If end is provided, start must be positive.");
        }

        if (end < start) {
            throw new IllegalArgumentException("end must be >= start.");
        }
    }

    protected HttpFileByteRange(long start, Long end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Indicates whether or not this byte range indicates an end.
     *
     * @return true if byte range has an end.
     */
    public boolean hasEnd() {
        return end != null;
    }

    /**
     * Get start index of byte range.
     *
     * @return Start index of byte range.
     */
    public long getStart() {
        return start;
    }

    /**
     * Get end index of byte range.
     *
     * @return End index of byte range.
     * @throws IllegalStateException if byte range does not have an end range.
     */
    public long getEnd() {
        if (!hasEnd()) {
            throw new IllegalStateException("Byte-range does not have end.  Check hasEnd() before use");
        }
        return end;
    }

    /**
     * Format byte range for use in header.
     */
    @Override
    public String toString() {
        if (end != null) {
            return BYTES_UNIT + "=" + start + "-" + end;
        } else {
            if (start < 0) {
                return BYTES_UNIT + "=" + start;
            } else {
                return BYTES_UNIT + "=" + start + "-";
            }
        }
    }

    /**
     * Parse byte range from header.
     *
     * @param byteRange Byte range string as received from header.
     * @return HttpFileByteRange object set to byte range as parsed from string.
     * @throws RangeFormatException            Unable to parse header because of invalid format.
     * @throws UnsupportedRangeFormatException Header is a valid HTTP range header, the specific
     *                                         form is not supported by app engine.  This includes unit types other than "bytes" and multiple
     *                                         ranges.
     */
    public static HttpFileByteRange parse(String byteRange) {
        Matcher matcher = RANGE_HEADER_PATTERN.matcher(byteRange);
        if (!matcher.matches()) {
            throw new RangeFormatException("Invalid range format: " + byteRange);
        }

        String unsupportedRange = matcher.group(4);
        if (!"".equals(unsupportedRange)) {
            throw new UnsupportedRangeFormatException("Unsupported range format: " + byteRange);
        }

        String units = matcher.group(1);
        if (!BYTES_UNIT.equals(units)) {
            throw new UnsupportedRangeFormatException("Unsupported unit: " + units);
        }

        String start = matcher.group(2);
        Long startValue;
        if ("".equals(start)) {
            startValue = null;
        } else {
            startValue = Long.parseLong(start);
        }

        String end = matcher.group(3);
        Long endValue;
        if ("".equals(end)) {
            endValue = null;
        } else {
            endValue = Long.parseLong(end);
        }

        if (startValue == null && endValue != null) {
            startValue = -endValue;
            endValue = null;
        }

        if (endValue == null) {
            return new HttpFileByteRange(startValue);
        } else {
            try {
                return new HttpFileByteRange((long) startValue, (long) endValue);
            } catch (IllegalArgumentException ex) {
                throw new RangeFormatException("Invalid range format: " + byteRange, ex);
            }
        }
    }

    /**
     * Parse content range from header for byte-range only.
     *
     * Exception throws if no end value, such as "bytes=123-".
     * If no end value, use parse(String) instead of this method.
     *
     * @param contentRange Content range string as received from header.
     * @return HttpFileByteRange object set to byte range as parsed from string, but does not include the
     * size information.
     * @throws RangeFormatException Unable to parse header because of invalid format.
     */
    public static HttpFileByteRange parseContentRange(String contentRange) {
        Matcher matcher = CONTENT_RANGE_HEADER_PATTERN.matcher(contentRange);
        if (!matcher.matches()) {
            throw new RangeFormatException("Invalid content-range format: " + contentRange);
        }

        return new HttpFileByteRange(Long.parseLong(matcher.group(1)), Long.parseLong(matcher.group(2)));
    }

    public static boolean isFileByteRangeFromStart(String fileRange) {
        HttpFileByteRange fileByteRange = httpFileByteRangeFromString(fileRange);

        return (fileByteRange != null && fileByteRange.getStart() == 0);
    }

    public static HttpFileByteRange httpFileByteRangeFromString(String fileRange) {
        HttpFileByteRange value;

        try {
            value = HttpFileByteRange.parse(fileRange);
        } catch (Exception e) {
            value = null;

            LOGGER.error("Failed to get value of http header File-Range: " + fileRange, e);
        }

        return value;
    }

    public static boolean validFileRange(String fileRange) {
        boolean valid;

        try {
            HttpFileByteRange.parse(fileRange);

            valid = true;
        } catch (Exception e) {
            valid = false;

            LOGGER.error("Invalid value of http header File-Range: " + fileRange, e);
        }

        return valid;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 37 + ((Long) start).hashCode();
        if (end != null) {
            hash = hash * 37 + end.hashCode();
        }
        return hash;
    }

    /**
     * Two {@code HttpFileByteRange} objects are considered equal if they have the same start and end.
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof HttpFileByteRange) {
            HttpFileByteRange key = (HttpFileByteRange) object;
            if (start != key.getStart()) {
                return false;
            }

            if (hasEnd() != key.hasEnd()) {
                return false;
            }

            if (hasEnd()) {
                return end.equals(key.getEnd());
            } else {
                return true;
            }
        }

        return false;
    }
}
