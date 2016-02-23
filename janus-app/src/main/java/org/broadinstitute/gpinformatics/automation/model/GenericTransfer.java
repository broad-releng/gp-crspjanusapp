package org.broadinstitute.gpinformatics.automation.model;

/**
 * interface for a generic tube to tube transfer
 */
public interface GenericTransfer {
    String getSourceWell();
    String getSourceBarcode();
    double getNewSourceVolume();
    double getSourceConcentration();

    String getDestinationWell();
    String getDestinationBarcode();
    double getDestinationVolume();
    double getDestinationConcentration();
}
