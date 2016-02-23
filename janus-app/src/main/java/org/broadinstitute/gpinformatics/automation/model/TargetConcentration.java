package org.broadinstitute.gpinformatics.automation.model;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Calendar;
import java.util.concurrent.Callable;

/**
 * For binding forms that ask for target conc
 */
public class TargetConcentration {
    public static final String VALID_NUMBER = "\\d+(\\.\\d+)?";

    private StringProperty targetConcentration = new SimpleStringProperty("");
    private BooleanBinding valid;

    /**
     * valid if targetConcentration is a decimal
     */
    public TargetConcentration() {
        valid = Bindings.createBooleanBinding(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return !targetConcentration.get().matches(VALID_NUMBER);
            }
        }, targetConcentration);
    }

    public double getTargetConcentration() {
        return Double.parseDouble(targetConcentration.get());
    }

    public StringProperty targetConcentrationProperty() {
        return targetConcentration;
    }

    public void setTargetConcentration(String targetConcentration) {
        this.targetConcentration.set(targetConcentration);
    }

    public BooleanBinding isValid() {
        return valid;
    }

    public void setValid(BooleanBinding valid) {
        this.valid = valid;
    }

    public void reset() {
        setTargetConcentration("");
    }
}
