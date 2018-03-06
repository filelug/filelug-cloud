package org.clopuccino.service;

/**
 * {@code UnsupportedRangeFormatException} is an unchecked exception that is thrown
 * when an valid but unsupported Range header format is provided.
 *
 */
public class UnsupportedRangeFormatException extends RangeFormatException {
    public UnsupportedRangeFormatException(String message) {
        super(message);
    }

    public UnsupportedRangeFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
