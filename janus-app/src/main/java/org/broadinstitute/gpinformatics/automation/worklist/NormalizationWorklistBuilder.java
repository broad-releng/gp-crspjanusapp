package org.broadinstitute.gpinformatics.automation.worklist;

import org.broadinstitute.gpinformatics.automation.model.Normalization;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.TargetConcentration;
import org.broadinstitute.gpinformatics.automation.model.Tube;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
import org.broadinstitute.techdev.lims.mercury.ConcentrationAndVolumeAndWeightType;
import org.broadinstitute.techdev.lims.mercury.LimsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Build worklist that will normalize the rack to the target concentration
 */
public class NormalizationWorklistBuilder implements WorklistBuilder {
    private final Rack rack;
    private final TargetConcentration targetConcentration;
    private final LimsService limsService;

    public NormalizationWorklistBuilder(Rack rack, TargetConcentration targetConcentration, LimsService limsService) {

        this.rack = rack;
        this.targetConcentration = targetConcentration;
        this.limsService = limsService;
    }

    @Override
    public List<WorklistRow> build() {
        List<WorklistRow> normalizations = new ArrayList<>();
        Map<String, ConcentrationAndVolumeAndWeightType> concentrationAndVolumeAndWeightTypes =
                limsService.fetchConcentrationAndVolumeAndWeightForTubeBarcodes(rack.getTubeBarcodes());
        Map<String, Tube> barcodeToTube = rack.getBarcodeToTube();
        double c2 = targetConcentration.getTargetConcentration();
        for(Map.Entry<String, ConcentrationAndVolumeAndWeightType> entry:
                concentrationAndVolumeAndWeightTypes.entrySet()) {
            ConcentrationAndVolumeAndWeightType cav = entry.getValue();
            double v1 = cav.getVolume();
            double c1 = cav.getConcentration();
            double eb = ((c1 * v1) / c2) - v1;
            double v2 = v1 + eb;
            String barcode = cav.getTubeBarcode();
            Tube tube = barcodeToTube.get(barcode);
            String well = tube.getWell();
            boolean wasInSpecification = cav.getConcentration() <= c2;
            Normalization normalization = new Normalization(true, wasInSpecification);
            normalization.setBarcode(cav.getTubeBarcode());
            normalization.setV1(cav.getVolume());
            normalization.setC1(cav.getConcentration());
            normalization.setWell(well);
            normalization.setTarget(c2);
            normalizations.add(normalization);
        }
        return normalizations;
    }
}
