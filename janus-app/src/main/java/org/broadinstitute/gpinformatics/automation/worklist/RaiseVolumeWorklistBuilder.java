package org.broadinstitute.gpinformatics.automation.worklist;

import org.broadinstitute.gpinformatics.automation.model.Normalization;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.TargetVolume;
import org.broadinstitute.gpinformatics.automation.model.Tube;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
import org.broadinstitute.techdev.lims.mercury.ConcentrationAndVolumeAndWeightType;
import org.broadinstitute.techdev.lims.mercury.LimsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Build worklist to raise volume of every tube under target to that target
 */
public class RaiseVolumeWorklistBuilder implements WorklistBuilder {

    private Rack rack;
    private LimsService limsService;
    private TargetVolume targetVolume;

    public RaiseVolumeWorklistBuilder(Rack rack, LimsService limsService,
                                      TargetVolume targetVolume) {
        this.rack = rack;
        this.limsService = limsService;
        this.targetVolume = targetVolume;
    }

    @Override
    public List<WorklistRow> build() {
        List<WorklistRow> normalizations = new ArrayList<>();
        Map<String, ConcentrationAndVolumeAndWeightType> concentrationAndVolumeAndWeightTypes =
                limsService.fetchConcentrationAndVolumeAndWeightForTubeBarcodes(rack.getTubeBarcodes());

        Map<String, Tube> barcodeToTube = rack.getBarcodeToTube();
        double target = targetVolume.getTargetVolume();
        for(Map.Entry<String, ConcentrationAndVolumeAndWeightType> entry:
                concentrationAndVolumeAndWeightTypes.entrySet()) {
            ConcentrationAndVolumeAndWeightType cav = entry.getValue();
            boolean wasInSpecification = cav.getVolume() >= target;
            Normalization normalization = new Normalization(false, wasInSpecification);
            Tube tube = barcodeToTube.get(cav.getTubeBarcode());
            normalization.setBarcode(tube.getBarcode());
            normalization.setWell(tube.getWell());
            double v1 = cav.getVolume();
            double c1 = cav.getConcentration();
            normalization.setV1(v1);
            normalization.setC1(c1);
            normalization.setTarget(target);
            normalizations.add(normalization);
        }
        return normalizations;
    }
}
