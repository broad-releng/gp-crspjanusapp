package org.broadinstitute.gpinformatics.automation.model.validation;

import javafx.beans.property.DoubleProperty;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.techdev.lims.mercury.ConcentrationAndVolumeAndWeightType;
import org.broadinstitute.techdev.lims.mercury.LimsService;

import java.util.Map;

/**
 * If daughter is going to require Spikes, then a parent rack scan needs to be present
 */
public class SpikeRequiresParentsValidation implements Validation{
    private final Rack parent;
    private final Rack daughter;
    private final DoubleProperty spikeConcentrationMax;
    private final LimsService limsService;

    public SpikeRequiresParentsValidation(Rack parent,
                                          Rack daughter,
                                          DoubleProperty spikeConcentrationMax,
                                          LimsService limsService) {

        this.parent = parent;
        this.daughter = daughter;
        this.spikeConcentrationMax = spikeConcentrationMax;
        this.limsService = limsService;
    }

    @Override
    public boolean isValid() {
        if(needsSpikes()) {
            return !parent.getBarcode().isEmpty() &&
                   !parent.isEmpty() &&
                   !parent.getBarcode().equals(daughter.getBarcode());
        }

        return true;
    }

    private boolean needsSpikes() {
        Map<String, ConcentrationAndVolumeAndWeightType> concentrationAndVolumeAndWeightTypeMap =
                limsService.fetchConcentrationAndVolumeAndWeightForTubeBarcodes(daughter.getTubeBarcodes());
        for (ConcentrationAndVolumeAndWeightType cAndV :concentrationAndVolumeAndWeightTypeMap.values()) {
            if(cAndV.getConcentration() < spikeConcentrationMax.get())
                return true;
        }
        return false;
    }

    @Override
    public IllegalArgumentException getException() {
        return new IllegalArgumentException("Spikes required. Parent Rack needs to be scanned");
    }
}
