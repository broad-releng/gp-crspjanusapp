package org.broadinstitute.gpinformatics.automation.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Convenience controller to replace panes in a stackpane easier
 */
public class StackPaneController implements Initializable {
    @FXML
    public StackPane stackPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        assert stackPane != null : "stackPane was not injected by FXML";
    }

    /**
     * Replace current pane with new pane in stack
     *
     * @param pane - Pane in stack to become current
     */
    public void replaceContent(Pane pane) {
        stackPane.getChildren().clear();
        stackPane.getChildren().add(pane);
    }
}
