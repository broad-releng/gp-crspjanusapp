package org.broadinstitute.gpinformatics.automation.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.joda.time.LocalDate;

import java.util.Calendar;

/**
 * Model reagent information
 */
public class Reagent {
    private String reagentType;
    private ObjectProperty<Calendar> expirationDate = new SimpleObjectProperty<>();
    private StringProperty barcode = new SimpleStringProperty("");

    public Reagent(String reagentType) {
        this.reagentType = reagentType;
    }

    public String getReagentType() {
        return reagentType;
    }

    public void setReagentType(String reagentType) {
        this.reagentType = reagentType;
    }

    public Calendar getExpirationDate() {
        return expirationDate.get();
    }

    public ObjectProperty<Calendar> expirationDateProperty() {
        return expirationDate;
    }

    public String getBarcode() {
        return barcode.get();
    }

    public StringProperty barcodeProperty() {
        return barcode;
    }

    public void setExpirationDate(Calendar expirationDate) {
        this.expirationDate.set(expirationDate);
    }

    public void setBarcode(String barcode) {
        this.barcode.set(barcode);
    }

    public Optional<String> hasErrors() {
        if(getExpirationDate() == null) {
            return Optional.of("Reagent Expiration is required");
        }

        LocalDate reagentExpirationLocalDate = new LocalDate(getExpirationDate().getTime());
        LocalDate currentLocalDate = new LocalDate();
        if(currentLocalDate.isAfter(reagentExpirationLocalDate) || currentLocalDate.isEqual(reagentExpirationLocalDate)) {
            return Optional.of("Reagent is expired");
        }

        if(getBarcode().isEmpty()) {
            return Optional.of("Reagent barcode is required");
        }

        return Optional.absent();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("reagentType", reagentType)
                .add("expirationDate", expirationDate)
                .add("barcode", barcode)
                .toString();
    }
}
