package org.broadinstitute.gpinformatics.automation.worklist;

import org.broadinstitute.gpinformatics.automation.model.DilutionTransfer;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.TargetVolumeAndConcentration;
import org.broadinstitute.gpinformatics.automation.model.Tube;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
import org.broadinstitute.techdev.lims.mercury.ConcentrationAndVolumeAndWeightType;
import org.broadinstitute.techdev.lims.mercury.LimsService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Build worklist that will perform a dilution transfer
 * from source rack to an empty destination rack
 */
public class DilutionWorklistBuilder implements WorklistBuilder {
    private final Rack sourceRack;
    private final Rack destinationRack;
    private final LimsService limsService;
    private final TargetVolumeAndConcentration targetVolumeAndConcentration;

    public DilutionWorklistBuilder(Rack sourceRack, Rack destinationRack,
                                   TargetVolumeAndConcentration targetVolumeAndConcentration,
                                   LimsService limsService) {
        this.sourceRack = sourceRack;
        this.destinationRack = destinationRack;
        this.targetVolumeAndConcentration = targetVolumeAndConcentration;
        this.limsService = limsService;
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

        double v2 = targetVolumeAndConcentration.getTargetVolume();
        double c2 = targetVolumeAndConcentration.getTargetConcentration();
        for(Map.Entry<String, ConcentrationAndVolumeAndWeightType> entry:
                concentrationAndVolumeAndWeightTypes.entrySet()) {
            ConcentrationAndVolumeAndWeightType cav = entry.getValue();
            Tube tube = barcodeToTube.get(cav.getTubeBarcode());
            String destinationTubeBarcode =
                    destinationRack.getTubeAt(tube.getRow(), tube.getColumn());
            double v1 = cav.getVolume();
            double c1 = cav.getConcentration();
            double dna = v2 * c2 / c1;
            double eb = v2 - dna;
            double newSourceVolume = v1 - dna;
            DilutionTransfer transfer = new DilutionTransfer();
            transfer.setSourceBarcode(tube.getBarcode());
            transfer.setDna(dna);
            transfer.setTe(eb);
            transfer.setNewSourceVolume(newSourceVolume);
            transfer.setSourceConcentration(cav.getConcentration());
            transfer.setTargetVolume(v2);
            transfer.setSourceWell(tube.getWell());
            transfer.setSourceVolume(v1);
            transfer.setTargetConcentration(c2);
            transfer.setDestinationWell(tube.getWell());
            transfer.setDestinationBarcode(destinationTubeBarcode);
            transfers.add(transfer);
        }

        return transfers;
    }
}
