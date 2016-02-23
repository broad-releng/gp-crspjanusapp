package org.broadinstitute.gpinformatics.automation.model.validation;

import org.broadinstitute.gpinformatics.automation.model.Rack;

public class BarcodeValidation implements Validation {

    private final String sourceBarcode;
    private final String destinationBarcode;

    public BarcodeValidation(String sourceBarcode, String destinationBarcode) {
        this.sourceBarcode = sourceBarcode;
        this.destinationBarcode = destinationBarcode;
    }

    @Override
    public boolean isValid() {
        if(sourceBarcode == null  || destinationBarcode == null)
            return false;
        if(sourceBarcode.isEmpty() || destinationBarcode.isEmpty())
            return false;
        return !sourceBarcode.equals(destinationBarcode);
    }

    @Override
    public IllegalArgumentException getException() {
        return new IllegalArgumentException("Rack barcodes need to be unique");
    }
}
