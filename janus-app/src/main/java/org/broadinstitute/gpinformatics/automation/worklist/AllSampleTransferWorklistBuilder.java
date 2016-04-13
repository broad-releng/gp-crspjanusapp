package org.broadinstitute.gpinformatics.automation.worklist;

import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.Tube;
import org.broadinstitute.gpinformatics.automation.model.VolumeTransfer;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
import org.broadinstitute.techdev.lims.mercury.ConcentrationAndVolumeAndWeightType;
import org.broadinstitute.techdev.lims.mercury.LimsService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Build worklist that will transfer the entire contents
 * from the source rack to an empty destination rack
 */
public class AllSampleTransferWorklistBuilder implements WorklistBuilder {
    private final Rack sourceRack;
    private final Rack destinationRack;
    private final LimsService limsService;

    public AllSampleTransferWorklistBuilder(Rack sourceRack, Rack destinationRack, LimsService limsService) {
        this.sourceRack = sourceRack;
        this.destinationRack = destinationRack;
        this.limsService = limsService;
    }

    /**
     * Treat this transfer as a Dilution where the target volume and concentrations are the sample
     * V1 and C1 values. This will let the user edit the table if they choose to do so.
     */
    @Override
    public List<WorklistRow> build() {
        List<WorklistRow> transfers = new ArrayList<>();
        List<String> tubeBarcodes = new ArrayList<>();
        Map<String, Tube> barcodeToTube = new HashMap<>();
        for(Tube tube : sourceRack){
            if (tube != null){
                if(tube.getBarcode() != null){
                    tubeBarcodes.add(tube.getBarcode());
                    barcodeToTube.put(tube.getBarcode(), tube);
                }
            }
        }

        Map<String, ConcentrationAndVolumeAndWeightType> concentrationAndVolumeAndWeightTypes =
                limsService.fetchConcentrationAndVolumeAndWeightForTubeBarcodes(tubeBarcodes);

        for(Map.Entry<String, ConcentrationAndVolumeAndWeightType> entry:
                concentrationAndVolumeAndWeightTypes.entrySet()) {
            ConcentrationAndVolumeAndWeightType cav = entry.getValue();
            Tube tube = barcodeToTube.get(cav.getTubeBarcode());
            String destinationTubeBarcode =
                    destinationRack.getTubeAt(tube.getRow(), tube.getColumn());
            double v1 = cav.getVolume();
            VolumeTransfer transfer = new VolumeTransfer();
            transfer.setSourceBarcode(tube.getBarcode());
            transfer.setDna(v1);
            transfer.setNewSourceVolume(0.0);
            transfer.setSourceConcentration(cav.getConcentration());
            transfer.setSourceWell(tube.getWell());
            transfer.setSourceVolume(v1);
            transfer.setDestinationWell(tube.getWell());
            transfer.setDestinationBarcode(destinationTubeBarcode);
            transfers.add(transfer);
        }

        return transfers;
    }
}
