package org.broadinstitute.gpinformatics.automation.model;

/**
 * Simple interface to define worklist row as columns
 */
public interface WorklistRow{
    String[] toWorklistRow();
    String sortWell();
}
