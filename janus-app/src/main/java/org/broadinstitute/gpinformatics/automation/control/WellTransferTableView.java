package org.broadinstitute.gpinformatics.automation.control;

import javafx.scene.control.TableColumn;
import org.broadinstitute.gpinformatics.automation.model.WellTransferRow;

public class WellTransferTableView extends BaseTableView<WellTransferRow> {
    TableColumn<WellTransferRow, String> wellCol = new TableColumn<>("Well");
    TableColumn<WellTransferRow, String> barcodeCol = new TableColumn<>("Barcode");
    TableColumn<WellTransferRow, Double> v1Col = new TableColumn<>("V1");
    TableColumn<WellTransferRow, Double> c1Col = new TableColumn<>("C1");
    TableColumn<WellTransferRow, Double> targetConcCol = new TableColumn<>("Target Conc");
    TableColumn<WellTransferRow, Double> dnaCol = new TableColumn<>("DNA");
    TableColumn<WellTransferRow, Double> teCol = new TableColumn<>("TE");
    TableColumn<WellTransferRow, Double> v2Col = new TableColumn<>("New Source Vol");
    TableColumn<WellTransferRow, Double> destVolCol = new TableColumn<>("Dest Vol");
    TableColumn<WellTransferRow, Double> destConcCol = new TableColumn<>("Dest Conc");

    public WellTransferTableView() {
        setEditable(true);
        attachCellFactory(wellCol, "sourceWell", false, false);
        attachCellFactory(barcodeCol, "sourceBarcode", false, false);
        attachCellFactory(v1Col, "sourceVolume", true, false);
        attachCellFactory(c1Col, "sourceConcentration", true, false);
        attachCellFactory(targetConcCol, "targetConcentration", true, true, 125);
        attachCellFactory(dnaCol, "dna", true, false);
        attachCellFactory(teCol, "te", true, false);
        attachCellFactory(v2Col, "newSourceVolume", true, false);
        attachCellFactory(destVolCol, "destinationVolume", true, false);
        attachCellFactory(destConcCol, "destinationconcentration", true, false);
    }
}
