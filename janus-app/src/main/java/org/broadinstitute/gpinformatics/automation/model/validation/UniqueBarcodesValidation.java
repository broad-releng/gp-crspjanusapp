package org.broadinstitute.gpinformatics.automation.model.validation;

import org.broadinstitute.gpinformatics.automation.model.Rack;

/**
 * Source and Destination barcodes must not be equal
 */
public class UniqueBarcodesValidation implements Validation {

    private Rack source;
    private Rack destination;

    public UniqueBarcodesValidation(Rack source, Rack destination) {
        this.source = source;
        this.destination = destination;
    }

    @Override
    public boolean isValid() {
        if(source == null  || destination == null)
            return false;
        if(source.getBarcode() == null || destination.getBarcode() == null)
            return false;
        if(source.getBarcode().isEmpty() || destination.getBarcode().isEmpty())
            return false;
        return !source.getBarcode().equals(destination.getBarcode());
    }

    @Override
    public IllegalArgumentException getException() {
        return new IllegalArgumentException("Rack barcodes need to be unique");
    }
}
