package org.broadinstitute.gpinformatics.automation.layout;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

/**
 * Simple form for grabbing Rack Barcode
 */
public class BarcodeBox extends HBox {

    private TextField textField;
    private Label label;

    public BarcodeBox() {
        label = new Label("Barcode:");
        textField = new TextField();
        textField.setId("rackPaneText");
        textField.setMinHeight(20);
        getChildren().addAll(label, textField);
        setAlignment(Pos.CENTER);
        setSpacing(5);
    }

    public TextField getTextField() {
        return textField;
    }
}
