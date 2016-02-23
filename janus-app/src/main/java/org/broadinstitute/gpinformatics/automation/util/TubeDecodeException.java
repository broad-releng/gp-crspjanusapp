package org.broadinstitute.gpinformatics.automation.util;

/**
 * Signals that an attempt to decode a tube barcode has failed
 */
public class TubeDecodeException extends Exception {
    public TubeDecodeException(String message) {
        super(message);
    }
}
