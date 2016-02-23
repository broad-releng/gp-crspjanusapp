package org.broadinstitute.gpinformatics.automation.model.validation;

/**
 * provides simple way for validating transfers
 */
public interface Validation {
    boolean isValid();
    IllegalArgumentException getException();
}
