package org.broadinstitute.gpinformatics.automation.layout;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import org.broadinstitute.gpinformatics.automation.model.Plate;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom control to show Grid of wells Row x Column
 * and a plate Barcode as well as fire rack scanner.
 */
public class PlatePane extends VBox {
    private static final Logger gLog = LoggerFactory.getLogger(RackPane.class);

    private Label plateNameLabel;
    private GridPane wellGridPane;
    private Plate plate;

    // FXML Properties
    private int rows;
    private int columns;
    private String plateName;
    private boolean source;

    public PlatePane() {
        plateNameLabel = new Label();
        wellGridPane = new GridPane();
        plate = new Plate();

        BarcodeBox barcodeBox = new BarcodeBox();
        Bindings.bindBidirectional(barcodeBox.getTextField().textProperty(), plate.getBarcodeProperty());

        getChildren().addAll(plateNameLabel, barcodeBox, wellGridPane);
        setPrefHeight(250);
        setPrefWidth(400);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(10, 0, 10, 0));
        setSpacing(10);
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public String getPlateName() {
        return plateName;
    }

    public void setPlateName(String plateName) {
        this.plateName = plateName;
    }

    public boolean isSource() {
        return source;
    }

    public void setSource(boolean source) {
        this.source = source;
    }

    @Override
    protected void layoutChildren() {
        initializeGridPane();
        initializeRackNameLabel();
        super.layoutChildren();
    }

    private void initializeRackNameLabel() {
        plateNameLabel.setText(plateName);
        plateNameLabel.setAlignment(Pos.CENTER);
    }

    /**
     * Build the Row x Col Grid of Rectangles.
     * Bind each rectangle to the rack such that if a rack has a
     * tube at [Row,Col], color it so the user can see
     */
    private void initializeGridPane() {
        wellGridPane.setHgap(.5);
        wellGridPane.setVgap(.5);
        wellGridPane.setAlignment(Pos.CENTER);
        for(int row = 0; row < getRows(); row++) {
            for(int col = 0; col < getColumns(); col++) {
                final StringProperty property = plate.getWellPropertyAt(row, col);
                BooleanBinding hasSample = property.isNotNull();

                Rectangle rect = RectangleBuilder.create()
                        .width(8)
                        .height(8)
                        .stroke(Color.BLACK)
                        .strokeWidth(.2)
                        .build();
                rect.fillProperty().bind(
                        Bindings.when(hasSample)
                                .then(Color.LIGHTSTEELBLUE)
                                .otherwise(Color.WHITE)
                );
                wellGridPane.add(rect, col, row);
            }
        }
    }

    public Plate getPlate() {
        return plate;
    }
}
