package org.broadinstitute.gpinformatics.automation.model.validation;

import org.broadinstitute.gpinformatics.automation.model.Rack;

/**
 * Rack must have barcode and have at least 1 tube
 */
public class RackValidation implements Validation {
    private Rack rack;

    public RackValidation(Rack rack) {
        this.rack = rack;
    }

    @Override
    public boolean isValid() {
        return !rack.getBarcode().isEmpty() &&
               !rack.isEmpty();
    }

    @Override
    public IllegalArgumentException getException() {
        return new IllegalArgumentException("Rack is not valid. Barcode and tubes required.");
    }
}
