package org.broadinstitute.gpinformatics.automation.model;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.concurrent.Callable;

/**
 * For property binding forms that ask for both target volume and
 * concentration
 */
public class TargetVolumeAndConcentration extends TargetConcentration {

    private StringProperty targetVolume = new SimpleStringProperty("");

    /**
     * Object is valid if target volume and target conc are both valid decimals
     */
    public TargetVolumeAndConcentration() {
        BooleanBinding valid = Bindings.createBooleanBinding(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return !targetVolume.get().matches(VALID_NUMBER) ||
                       !targetConcentrationProperty().get().matches(VALID_NUMBER);
            }
        }, targetVolumeProperty(), targetConcentrationProperty());
        setValid(valid);
    }

    public double getTargetVolume() {
        return Double.parseDouble(targetVolume.get());
    }

    public void setTargetVolume(String targetVolume) {
        this.targetVolume.setValue(targetVolume);
    }

    public StringProperty targetVolumeProperty() {
        return targetVolume;
    }

    public void reset() {
        super.reset();
        setTargetVolume("");
    }
}
