package org.broadinstitute.gpinformatics.automation.control;

import javafx.scene.control.TableColumn;
import org.broadinstitute.gpinformatics.automation.model.Normalization;

/**
 * Build Tableview for the Normalization model type
 */
public class NormalizationTableView extends BaseTableView<Normalization> {
    TableColumn<Normalization, String> wellCol = new TableColumn<>("Well");
    TableColumn<Normalization, Double> v1Col = new TableColumn<>("V1");
    TableColumn<Normalization, Double> c1Col = new TableColumn<>("C1");
    TableColumn<Normalization, Double> targetCol = new TableColumn<>("Target");
    TableColumn<Normalization, Double> teCol = new TableColumn<>("TE");
    TableColumn<Normalization, Double> v2Col = new TableColumn<>("V2");
    TableColumn<Normalization, Double> c2Col = new TableColumn<>("C2");

    public NormalizationTableView() {
        setEditable(true);
        attachCellFactory(wellCol, "well", false, false, 50);
        attachCellFactory(v1Col, "v1", true, false, 50);
        attachCellFactory(c1Col, "c1", true, false, 50);
        attachCellFactory(targetCol, "target", true, true, 100);
        attachCellFactory(teCol, "te", true, false, 50);
        attachCellFactory(v2Col, "v2", true, false, 50);
        attachCellFactory(c2Col, "c2", true, false, 50);
    }
}
