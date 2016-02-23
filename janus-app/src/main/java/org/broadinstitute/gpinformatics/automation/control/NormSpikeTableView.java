package org.broadinstitute.gpinformatics.automation.control;

import javafx.scene.control.TableColumn;
import org.broadinstitute.gpinformatics.automation.model.NormalizationAndSpikeRow;
import org.broadinstitute.gpinformatics.automation.model.TransferType;

/**
 * Data table for TruSeq Norm Spike protocol
 */
public class NormSpikeTableView extends BaseTableView<NormalizationAndSpikeRow> {
    TableColumn<NormalizationAndSpikeRow, TransferType> transferTypeCol = new TableColumn<>("Type");
    TableColumn<NormalizationAndSpikeRow, String> daughterWellCol = new TableColumn<>("Daughter Well");
    TableColumn<NormalizationAndSpikeRow, Double> parentVolumeCol = new TableColumn<>("Parent Vol");
    TableColumn<NormalizationAndSpikeRow, Double> parentConcentrationCol = new TableColumn<>("Parent Conc");
    TableColumn<NormalizationAndSpikeRow, Double> daughterVolumeCol = new TableColumn<>("Daughter Vol");
    TableColumn<NormalizationAndSpikeRow, Double> daughterConcentrationCol = new TableColumn<>("Daughter Conc");
    TableColumn<NormalizationAndSpikeRow, Double> newDaughterVolumeCol = new TableColumn<>("V2");
    TableColumn<NormalizationAndSpikeRow, Double> newDaughterConcentrationCol = new TableColumn<>("C2");
    TableColumn<NormalizationAndSpikeRow, Double> newParentVolumeCol = new TableColumn<>("Parent V2");
    TableColumn<NormalizationAndSpikeRow, Double> dnaCol = new TableColumn<>("Sample to Add");
    TableColumn<NormalizationAndSpikeRow, Double> spikeTargetConcentrationCol = new TableColumn<>("Spike Target");
    TableColumn<NormalizationAndSpikeRow, Double> normTargetConcentrationCol = new TableColumn<>("Norm Target");
    TableColumn<NormalizationAndSpikeRow, Double> teCol = new TableColumn<>("TE");

    public NormSpikeTableView() {
        setEditable(true);
        attachCellFactory(transferTypeCol, "transferType", false, false);
        attachCellFactory(daughterWellCol, "daughterWell", false, false);
        attachCellFactory(parentVolumeCol, "parentVolume", false, false);
        attachCellFactory(parentConcentrationCol, "parentConcentration", false, false);
        attachCellFactory(daughterVolumeCol, "daughterVolume", false, false);
        attachCellFactory(daughterConcentrationCol, "daughterConcentration", false, false);
        attachCellFactory(newDaughterVolumeCol, "newDaughterVolume", false, false);
        attachCellFactory(newDaughterConcentrationCol, "newDaughterConcentration", false, false);
        attachCellFactory(newParentVolumeCol, "newParentVolume", false, false);
        attachCellFactory(dnaCol, "dna", false, false);
        attachCellFactory(spikeTargetConcentrationCol, "spikeTargetConcentration", false, false);
        attachCellFactory(normTargetConcentrationCol, "normTargetConcentration", false, false);
        attachCellFactory(teCol, "te", false, false);
    }
}
