package org.broadinstitute.gpinformatics.automation.model;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.validation.constraints.Min;
import java.util.concurrent.Callable;

/**
 * For binding forms that ask for target volume
 */
public class TargetVolume {
    public static final String VALID_NUMBER = "\\d+(\\.\\d+)?";

    private StringProperty targetVolume = new SimpleStringProperty("");

    private BooleanBinding valid;

    /**
     * valid if targetVolume is a decimal
     */
    public TargetVolume() {
        valid = Bindings.createBooleanBinding(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return !targetVolume.get().matches(VALID_NUMBER);
            }
        }, targetVolume);
    }

    @Min(0)
    public double getTargetVolume() {
        return Double.parseDouble(targetVolume.get());
    }

    public StringProperty targetVolumeProperty() {
        return targetVolume;
    }

    public void setTargetVolume(String targetConcentration) {
        this.targetVolume.set(targetConcentration);
    }

    public BooleanBinding isValid() {
        return valid;
    }

    public void reset() {
        setTargetVolume("");
    }

    @Override
    public String toString() {
        return "TargetVolume{" +
               "targetVolume=" + targetVolume +
               ", valid=" + valid +
               '}';
    }
}
