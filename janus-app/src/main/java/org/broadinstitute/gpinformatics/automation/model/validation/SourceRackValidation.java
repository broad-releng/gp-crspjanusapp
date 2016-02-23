package org.broadinstitute.gpinformatics.automation.model.validation;

import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.techdev.lims.mercury.LimsService;

import java.util.List;

/**
 * Validates that source rack is known in lims
 */
public class SourceRackValidation implements Validation {

    private Rack rack;
    private LimsService limsService;

    public SourceRackValidation(Rack rack, LimsService limsService) {
        this.rack = rack;
        this.limsService = limsService;
    }

    @Override
    public boolean isValid() {
        if(rack == null)
            return false;

        List<String> barcodes = rack.getTubeBarcodes();
        return limsService.doesLimsRecognizeAllTubes(barcodes);
    }

    @Override
    public IllegalArgumentException getException() {
        return new IllegalArgumentException("Source tubes are not registered in lims");
    }
}
