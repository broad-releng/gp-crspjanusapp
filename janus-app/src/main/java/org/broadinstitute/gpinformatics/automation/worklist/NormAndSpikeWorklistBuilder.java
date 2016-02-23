package org.broadinstitute.gpinformatics.automation.worklist;

import javafx.beans.property.DoubleProperty;
import org.broadinstitute.gpinformatics.automation.model.NormalizationAndSpikeRow;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.TargetConcentration;
import org.broadinstitute.gpinformatics.automation.model.TransferType;
import org.broadinstitute.gpinformatics.automation.model.Tube;
import org.broadinstitute.gpinformatics.automation.model.WorklistRow;
import org.broadinstitute.techdev.lims.mercury.ConcentrationAndVolumeAndWeightType;
import org.broadinstitute.techdev.lims.mercury.LibraryDataType;
import org.broadinstitute.techdev.lims.mercury.LimsService;
import org.broadinstitute.techdev.lims.mercury.SampleInfoType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Build worklist that will either perform a buffer addition on daughter Rack
 * or will spike in sample from the parent tube.
 */
public class NormAndSpikeWorklistBuilder implements WorklistBuilder {

    private final Rack parentRack;
    private final Rack daughterRack;
    private final TargetConcentration spikeTargetConcentration;
    private final TargetConcentration normTargetVolumeAndConcentration;
    private final DoubleProperty spikeMax;
    private final DoubleProperty normalizationMin;
    private final LimsService limsService;

    public NormAndSpikeWorklistBuilder(Rack sourceRack, Rack daughterRack,
                                       TargetConcentration spikeTargetConcentration,
                                       TargetConcentration normTargetConcentration,
                                   DoubleProperty spikeMax, DoubleProperty normalizationMin,
                                   LimsService limsService) {
        this.parentRack = sourceRack;
        this.daughterRack = daughterRack;
        this.spikeTargetConcentration = spikeTargetConcentration;
        this.normTargetVolumeAndConcentration = normTargetConcentration;
        this.spikeMax = spikeMax;
        this.normalizationMin = normalizationMin;
        this.limsService = limsService;
    }

    @Override
    public List<WorklistRow> build() {
        List<WorklistRow> rows = new ArrayList<>();
        Map<String, ConcentrationAndVolumeAndWeightType> daughtersCandV =
                limsService.fetchConcentrationAndVolumeAndWeightForTubeBarcodes(daughterRack.getTubeBarcodes());
        Map<String, Tube> daughterBarcodeToTube = daughterRack.getBarcodeToTube();

        Map<String, ConcentrationAndVolumeAndWeightType> parentsCandV = null;
        Map<String, String> parentSampleIdToBarcode = null;
        Map<String, Tube> parentBarcodeToTube = null;
        boolean needsSpikes = needsSpikes(daughtersCandV);
        if(needsSpikes) {
            if(parentRack == null || parentRack.isEmpty())
                throw new RuntimeException("Daughter Rack requires spikes. Parent Rack scan is required to continue");
            parentsCandV = limsService.fetchConcentrationAndVolumeAndWeightForTubeBarcodes(parentRack.getTubeBarcodes());
            parentSampleIdToBarcode = findParents();
            parentBarcodeToTube = parentRack.getBarcodeToTube();
        }

        for(Map.Entry<String, ConcentrationAndVolumeAndWeightType> entry:
                daughtersCandV.entrySet()) {
            ConcentrationAndVolumeAndWeightType daughterCV = entry.getValue();
            NormalizationAndSpikeRow row = new NormalizationAndSpikeRow(spikeMax.get(), normalizationMin.get());
            Tube daughterTube = daughterBarcodeToTube.get(daughterCV.getTubeBarcode());
            row.setDaughterConcentration(daughterCV.getConcentration());
            row.setDaughterVolume(daughterCV.getVolume());
            row.setDaughterBarcode(daughterCV.getTubeBarcode());
            row.setDaughterWell(daughterTube.getWell());
            row.setNormTargetConcentration(normTargetVolumeAndConcentration.getTargetConcentration());
            if(daughterCV.getConcentration() < spikeMax.get()) { //Spike
                row.setTransferType(TransferType.SPIKE);
                String daughterSampleId = fetchSampleNameFromTubeBarcode(daughterCV.getTubeBarcode());
                if(daughterSampleId == null)
                    throw new RuntimeException(
                            "Could not find sample ID for daughter tube: " + daughterCV.getTubeBarcode());
                if(!parentSampleIdToBarcode.containsKey(daughterSampleId)) {
                    throw new RuntimeException(
                            String.format("Could not find sample %s in parent rack", daughterSampleId));
                } else {
                    String parentTubeBarcode = parentSampleIdToBarcode.get(daughterSampleId);
                    Tube parentTube = parentBarcodeToTube.get(parentTubeBarcode);
                    ConcentrationAndVolumeAndWeightType parentCandV = parentsCandV.get(parentTubeBarcode);
                    row.setParentBarcode(parentTubeBarcode);
                    row.setParentWell(parentTube.getWell());
                    row.setParentConcentration(parentCandV.getConcentration());
                    row.setParentVolume(parentCandV.getVolume());
                    row.setSpikeTargetConcentration(spikeTargetConcentration.getTargetConcentration());
                    row.update();
                    rows.add(row);
                }
            } else if (daughterCV.getConcentration() > normalizationMin.get()) { //Norm
                row.setTransferType(TransferType.NORMALIZATION);
                row.update();
                rows.add(row);
            } else { //Sample is in spec, do nothing
                row.setTransferType(TransferType.NONE);
            }
        }

        return rows;
    }

    private Map<String, String> findParents() {
        Map<String, String> parentSampleIdToBarcode = new HashMap<>();
        for(String tubeBarcode: parentRack.getTubeBarcodes()) {
            String sampleId = fetchSampleNameFromTubeBarcode(tubeBarcode);
            parentSampleIdToBarcode.put(sampleId, tubeBarcode);
        }
        return parentSampleIdToBarcode;
    }

    private String fetchSampleNameFromTubeBarcode(String tubeBarcode) {
        List<LibraryDataType> libraryDataTypes =
                limsService.fetchLibraryDetailsByTubeBarcode(Arrays.asList(tubeBarcode), true);
        if(libraryDataTypes != null && !libraryDataTypes.isEmpty()) {
            LibraryDataType libraryDataType = libraryDataTypes.get(0);
            if(libraryDataType.isWasFound()) {
                List<SampleInfoType> sampleDetails = libraryDataType.getSampleDetails();
                if(sampleDetails != null && !sampleDetails.isEmpty()) {
                    SampleInfoType sampleInfoType = sampleDetails.get(0);
                    return sampleInfoType.getSampleName();
                }
            }
        }
        return null;
    }

    public boolean needsSpikes(Map<String, ConcentrationAndVolumeAndWeightType> concentrationAndVolumeAndWeightTypeMap) {
        for(Map.Entry<String, ConcentrationAndVolumeAndWeightType> entry:
                concentrationAndVolumeAndWeightTypeMap.entrySet()) {
            if(entry.getValue().getConcentration() < spikeMax.doubleValue())
                return true;
        }
        return false;
    }
}
