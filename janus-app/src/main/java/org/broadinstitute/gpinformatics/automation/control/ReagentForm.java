package org.broadinstitute.gpinformatics.automation.control;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import jfxtras.labs.fxml.JFXtrasBuilderFactory;
import jfxtras.labs.scene.control.CalendarTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.ResourceBundle;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Custom Control for ReagentForm.fxml
 */
public class ReagentForm extends GridPane implements Initializable {
    private static final Logger gLog = LoggerFactory.getLogger(ReagentForm.class);
    private static final String REAGENT_FORM_FXML = "ReagentForm.fxml";

    @FXML
    public TextField barcodeTextField;

    @FXML
    public CalendarTextField datePicker;

    @FXML
    public String dateFormat;

    private ObjectProperty<Calendar> expirationDate = new SimpleObjectProperty<>();

    private StringProperty barcode = new SimpleStringProperty("");

    public ReagentForm() {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource(REAGENT_FORM_FXML));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        fxmlLoader.setBuilderFactory(new JFXtrasBuilderFactory());

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            gLog.error("ReagentForm: error initializing FXML", exception);
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        checkNotNull(barcodeTextField, "barcodeTextField not injected by FXML");
        checkNotNull(datePicker, "datePicker not injected by FXML");
        checkNotNull(dateFormat, "dateFormat not injected by FXML");

        expirationDate.bind(datePicker.calendarProperty());
        barcode.bind(barcodeTextField.textProperty());
    }

    public ObjectProperty<Calendar> expirationDateProperty() {
        return expirationDate;
    }

    public StringProperty barcodeProperty() {
        return barcode;
    }

    public void reset() {
        datePicker.setCalendar(null);
        barcodeTextField.setText("");
    }
}
