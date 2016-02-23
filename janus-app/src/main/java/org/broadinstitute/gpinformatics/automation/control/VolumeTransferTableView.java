package org.broadinstitute.gpinformatics.automation.control;

import javafx.scene.control.TableColumn;
import org.broadinstitute.gpinformatics.automation.model.DilutionTransfer;
import org.broadinstitute.gpinformatics.automation.model.VolumeTransfer;

/**
 * Data table for Volume Transfer protocol
 */
public class VolumeTransferTableView extends BaseTableView<VolumeTransfer> {
    TableColumn<DilutionTransfer, String> wellCol = new TableColumn<>("Well");
    TableColumn<DilutionTransfer, Double> v1Col = new TableColumn<>("V1");
    TableColumn<DilutionTransfer, Double> c1Col = new TableColumn<>("C1");
    TableColumn<DilutionTransfer, Double> dnaCol = new TableColumn<>("DNA");
    TableColumn<DilutionTransfer, Double> v2Col = new TableColumn<>("New Source Vol");
    TableColumn<DilutionTransfer, Double> destVolumeCol = new TableColumn<>("Dest Volume");
    TableColumn<DilutionTransfer, Double> destConcCol = new TableColumn<>("Dest Conc");

    public VolumeTransferTableView() {
        setEditable(true);
        attachCellFactory(wellCol, "sourceWell", false, false);
        attachCellFactory(v1Col, "sourceVolume", true, false);
        attachCellFactory(c1Col, "sourceConcentration", true, false);
        attachCellFactory(dnaCol, "dna", true, true);
        attachCellFactory(v2Col, "newSourceVolume", true, false);
        attachCellFactory(destVolumeCol, "destinationVolume", true, false);
        attachCellFactory(destConcCol, "destinationConcentration", true, false);
    }
}
