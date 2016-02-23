package org.broadinstitute.gpinformatics.automation.layout;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import org.broadinstitute.gpinformatics.automation.Alerts;
import org.broadinstitute.gpinformatics.automation.App;
import org.broadinstitute.gpinformatics.automation.model.Rack;
import org.broadinstitute.gpinformatics.automation.model.Tube;
import org.broadinstitute.gpinformatics.automation.util.FakeRackScanner;
import org.broadinstitute.gpinformatics.automation.util.RackScanner;
import org.broadinstitute.gpinformatics.automation.util.RuntimeExecutor;
import org.broadinstitute.gpinformatics.automation.util.ZiathRackScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Custom control to show Grid of tubes Row x Column
 * and a Rack Barcode as well as fire rack scanner.
 *
 * Possibly create an FXML custom control instead
 */
public class RackPane extends VBox {
    private static final Logger gLog = LoggerFactory.getLogger(RackPane.class);

    //Rack Scanner timeout
    private static final long TIMEOUT = 1000 * 60;

    private Label rackNameLabel;
    private GridPane tubeGridPane;
    private Button scanButton;
    private ProgressIndicator progressIndicator;
    private Rack rack;

    // FXML Properties
    private int rows;
    private int columns;
    private String rackName;
    private boolean source;

    private RackScanner rackScanner;

    public RackPane() {
        rows = 8;
        columns = 12;
        rackNameLabel = new Label();
        tubeGridPane = new GridPane();
        scanButton = new Button("Scan Rack");
        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        rack = new Rack(getRows(), getColumns());

        BarcodeBox rackBarcodeBox = new BarcodeBox();
        Bindings.bindBidirectional(rackBarcodeBox.getTextField().textProperty(), rack.getBarcodeProperty());

        getChildren().addAll(rackNameLabel, rackBarcodeBox, tubeGridPane, scanButton, progressIndicator);
        setPrefHeight(250);
        setPrefWidth(400);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(10, 0, 10, 0));
        setSpacing(10);
    }

    private void initRackScanner() {
        if(App.UI_TEST) {
            File file = null;
            if(isSource())
                file = App.sourceFile;
            else if(getRackName().contains("Fingerprint"))
                file = App.fingerprintDestinationFile;
            else
                file = App.shearingDestinationFile;
            rackScanner = new FakeRackScanner(file);
        } else {
            rackScanner = new ZiathRackScanner(new RuntimeExecutor(TIMEOUT));
        }
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

    public String getRackName() {
        return rackName;
    }

    public void setRackName(String rackName) {
        this.rackName = rackName;
    }

    public boolean isSource() {
        return source;
    }

    public void setSource(boolean source) {
        this.source = source;
    }

    @Override
    protected void layoutChildren() {
        assert rackName != null : "RackPanes rackName field is required";
        initializeGridPane();
        initializeScanButton();
        initializeRackNameLabel();
        initRackScanner();
        super.layoutChildren();
    }

    private void initializeRackNameLabel() {
        rackNameLabel.setText(rackName);
        rackNameLabel.setAlignment(Pos.CENTER);
    }

    private void initializeScanButton() {
        scanButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                final Task<Rack> task = new Task<Rack>() {

                    @Override
                    protected Rack call() throws Exception {
                        return rackScanner.fire();
                    }
                };

                scanButton.disableProperty().bind(task.runningProperty());
                progressIndicator.visibleProperty().bind(task.runningProperty());
                task.stateProperty().addListener(new ChangeListener<Worker.State>(){

                    @Override
                    public void changed(ObservableValue<? extends Worker.State> observableValue,
                                        Worker.State oldState,
                                        Worker.State newState) {
                        if(newState == Worker.State.SUCCEEDED) {
                            try {
                                handleRackScanSuccess(task.get());
                            } catch (Exception e) {
                                gLog.error("Rack scan task failed to complete", e);
                                handleRackScanFailure();
                            }
                        } else if(newState == Worker.State.FAILED) {
                            gLog.warn("Rack scan task failed");
                            handleRackScanFailure();
                        }
                    }
                });

                new Thread(task).start();
            }
        });
    }

    private void handleRackScanFailure() {
        gLog.warn("RackPane: Rack scan task failed");
        Alerts.error("Could not scan rack. Make sure that all of the tubes are set");
    }

    private void handleRackScanSuccess(Rack rack) {
        gLog.info("Rack scan task completed successfully");
        if(rack.getBarcode() != null && !rack.getBarcode().isEmpty()) {
            this.rack.setBarcode(rack.getBarcode());
        }
        for (Tube tube : rack) {
            this.rack.addTube(tube);
        }
    }

    /**
     * Build the Row x Col Grid of Rectangles.
     * Bind each rectangle to the rack such that if a rack has a
     * tube at [Row,Col], color it so the user can see
     */
    private void initializeGridPane() {
        tubeGridPane.setHgap(.5);
        tubeGridPane.setVgap(.5);
        tubeGridPane.setAlignment(Pos.CENTER);
        for(int row = 0; row < getRows(); row++) {
            for(int col = 0; col < getColumns(); col++) {
                Tooltip t = new Tooltip();
                StringProperty property = rack.getTubePropertyAt(row, col);
                BooleanBinding hasTube = property.isNotNull();
                t.textProperty().bind(
                        Bindings.when(hasTube)
                                .then(property)
                                .otherwise("EMPTY")
                );

                Rectangle rect = RectangleBuilder.create()
                        .width(20)
                        .height(20)
                        .stroke(Color.BLACK)
                        .strokeWidth(.25)
                        .build();
                rect.fillProperty().bind(
                        Bindings.when(hasTube)
                                .then(Color.LIGHTSTEELBLUE)
                                .otherwise(Color.WHITE)
                );
                Tooltip.install(rect, t);
                tubeGridPane.add(rect, col, row);
            }
        }
    }

    public Rack getRack() {
        return rack;
    }
}
