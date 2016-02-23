package org.broadinstitute.gpinformatics.automation.control;

import javafx.scene.control.TableColumn;
import org.broadinstitute.gpinformatics.automation.model.DilutionTransfer;

public class DilutionTableView extends BaseTableView<DilutionTransfer> {
    TableColumn<DilutionTransfer, String> wellCol = new TableColumn<>("Well");
    TableColumn<DilutionTransfer, String> barcodeCol = new TableColumn<>("Barcode");
    TableColumn<DilutionTransfer, Double> v1Col = new TableColumn<>("V1");
    TableColumn<DilutionTransfer, Double> c1Col = new TableColumn<>("C1");
    TableColumn<DilutionTransfer, Double> targetConcCol = new TableColumn<>("Target Conc");
    TableColumn<DilutionTransfer, Double> targetVolCol = new TableColumn<>("Target Vol");
    TableColumn<DilutionTransfer, Double> dnaCol = new TableColumn<>("DNA");
    TableColumn<DilutionTransfer, Double> teCol = new TableColumn<>("TE");
    TableColumn<DilutionTransfer, Double> v2Col = new TableColumn<>("New Source Vol");

    public DilutionTableView() {
        setEditable(true);
        attachCellFactory(wellCol, "sourceWell", false, false);
        attachCellFactory(barcodeCol, "sourceBarcode", false, false);
        attachCellFactory(v1Col, "sourceVolume", true, false);
        attachCellFactory(c1Col, "sourceConcentration", true, false);
        attachCellFactory(targetConcCol, "targetConcentration", true, true, 125);
        attachCellFactory(targetVolCol, "targetVolume", true, true, 125);
        attachCellFactory(dnaCol, "dna", true, false);
        attachCellFactory(teCol, "te", true, false);
        attachCellFactory(v2Col, "newSourceVolume", true, false);
    }
}