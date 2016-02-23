package org.broadinstitute.gpinformatics.automation.worklist;

import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.TargetVolume;
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
 * Build worklist that will perform a set volume ALL96 stamp
 * from source rack to the destination rack
 */
public class VolumeTransferWorklistBuilder implements WorklistBuilder {

    private final Rack sourceRack;
    private final Rack destinationRack;
    private final LimsService limsService;
    private final TargetVolume targetVolume;

    public VolumeTransferWorklistBuilder(Rack sourceRack, Rack destinationRack, LimsService limsService,
                                         TargetVolume targetVolume) {
        this.sourceRack = sourceRack;
        this.destinationRack = destinationRack;
        this.limsService = limsService;
        this.targetVolume = targetVolume;
    }

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

        double transferVolume = targetVolume.getTargetVolume();
        for(Map.Entry<String, ConcentrationAndVolumeAndWeightType> entry:
                concentrationAndVolumeAndWeightTypes.entrySet()) {
            ConcentrationAndVolumeAndWeightType cav = entry.getValue();
            Tube tube = barcodeToTube.get(cav.getTubeBarcode());
            String destinationTubeBarcode =
                    destinationRack.getTubeAt(tube.getRow(), tube.getColumn());
            double v1 = cav.getVolume();
            double c1 = cav.getConcentration();
            double newSourceVolume = v1 - transferVolume;
            VolumeTransfer transfer = new VolumeTransfer();
            transfer.setSourceBarcode(tube.getBarcode());
            transfer.setDna(transferVolume);
            transfer.setNewSourceVolume(newSourceVolume);
            transfer.setSourceWell(tube.getWell());
            transfer.setSourceVolume(v1);
            transfer.setDestinationWell(tube.getWell());
            transfer.setDestinationBarcode(destinationTubeBarcode);
            transfer.setSourceConcentration(c1);
            transfer.setDestinationConcentration(c1);
            transfer.setDestinationVolume(transferVolume);
            transfers.add(transfer);
        }

        return transfers;
    }
}
