package org.broadinstitute.gpinformatics.automation.model.validation;

import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.TargetVolume;
import org.broadinstitute.techdev.lims.mercury.ConcentrationAndVolumeAndWeightType;
import org.broadinstitute.techdev.lims.mercury.LimsService;

import java.util.List;
import java.util.Map;

/**
 * validates that enough volume is available in rack to transfer target amount
 */
public class VolumeCheck implements Validation {

    private final Rack rack;
    private final LimsService limsService;
    private final TargetVolume targetVolume;
    private String barcode;

    public VolumeCheck(Rack rack, LimsService limsService, TargetVolume targetVolume) {
        this.rack = rack;
        this.limsService = limsService;
        this.targetVolume = targetVolume;
    }

    @Override
    public boolean isValid() {
        if(rack == null)
            return false;

        List<String> barcodes = rack.getTubeBarcodes();
        Map<String, ConcentrationAndVolumeAndWeightType> concentrationAndVolumeAndWeightTypes =
                limsService.fetchConcentrationAndVolumeAndWeightForTubeBarcodes(barcodes);

        for(Map.Entry<String, ConcentrationAndVolumeAndWeightType> entry:
                concentrationAndVolumeAndWeightTypes.entrySet()) {
            if (entry.getValue().getVolume() - targetVolume.getTargetVolume() < 0) {
                barcode = entry.getKey();
                return false;
            }
        }

        return true;
    }

    @Override
    public IllegalArgumentException getException() {
        return new IllegalArgumentException("Not enough volume to perform transfer for: " + barcode);
    }
}
