package org.broadinstitute.gpinformatics.automation.worklist;

import org.broadinstitute.gpinformatics.automation.model.Plate;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.WellTransferRow;
import org.broadinstitute.gpinformatics.automation.model.TargetConcentration;
import org.broadinstitute.gpinformatics.automation.model.Tube;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.broadinstitute.gpinformatics.automation.util.WellUtil;
import org.broadinstitute.techdev.lims.mercury.*;

/**
 * Build worklist that will perform a dilution transfer
 * from source rack to an empty destination rack
 */
public class RNACaliperQCWorklistBuilder implements WorklistBuilder {
    private final Rack sourceRack;
    private final Plate destinationPlate;
    private final TargetConcentration targetConcentration;
    private final LimsService limsService;

    private final int STOCK_TRANSFER_VOL = 1;

    public RNACaliperQCWorklistBuilder(Rack sourceRack, Plate destinationPlate,
                                       TargetConcentration targetConcentration,
                                       LimsService limsService) {

        this.sourceRack = sourceRack;
        this.destinationPlate = destinationPlate;
        this.targetConcentration = targetConcentration;
        this.limsService = limsService;
    }

    @Override
    public List<WorklistRow> build() {
        List<WorklistRow> rows = new ArrayList<>();
        Map<String, ConcentrationAndVolumeAndWeightType> concentrationAndVolumeAndWeightTypes =
                limsService.fetchConcentrationAndVolumeAndWeightForTubeBarcodes(sourceRack.getTubeBarcodes());
        Map<String, Tube> barcodeToTube = sourceRack.getBarcodeToTube();
        double c2 = targetConcentration.getTargetConcentration();
        for(Map.Entry<String, ConcentrationAndVolumeAndWeightType> entry:
                concentrationAndVolumeAndWeightTypes.entrySet()) {
            ConcentrationAndVolumeAndWeightType cav = entry.getValue();
            double c1 = cav.getConcentration();
            double eb = ((c1 * STOCK_TRANSFER_VOL) / c2) - STOCK_TRANSFER_VOL;
            double v2 = STOCK_TRANSFER_VOL + eb;
            String barcode = cav.getTubeBarcode();
            Tube tube = barcodeToTube.get(barcode);
            String well = tube.getWell();
            String destWell = WellUtil.From96To384(well);
            WellTransferRow row = new WellTransferRow();
            row.setSourceBarcode(cav.getTubeBarcode());
            row.setSourceVolume(cav.getVolume());
            row.setSourceConcentration(cav.getConcentration());
            row.setSourceWell(well);
            row.setDestinationWell(destWell);
            row.setDestinationVolume(v2);
            row.setTargetConcentration(c2);
            rows.add(row);
        }
        return rows;
    }
}
