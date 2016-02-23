package org.broadinstitute.gpinformatics.automation.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.broadinstitute.gpinformatics.automation.util.WellUtil;

/**
 * Transfer a set amount of DNA and norm to a target concentration
 */
public class WellTransferRow implements WorklistRow {
    private final StringProperty sourceWell = new SimpleStringProperty("");
    private final StringProperty sourceBarcode = new SimpleStringProperty("");
    private final StringProperty destinationWell = new SimpleStringProperty("");
    private final DoubleProperty sourceVolume = new SimpleDoubleProperty();
    private final DoubleProperty sourceConcentration = new SimpleDoubleProperty();
    private final DoubleProperty targetConcentration = new SimpleDoubleProperty();
    private final DoubleProperty destinationVolume = new SimpleDoubleProperty();
    private final DoubleProperty destinationconcentration = new SimpleDoubleProperty();
    private final DoubleProperty newSourceVolume = new SimpleDoubleProperty();
    private final DoubleProperty dna = new SimpleDoubleProperty();
    private final DoubleProperty te = new SimpleDoubleProperty();

    public String getSourceWell() {
        return this.sourceWell.get();
    }

    public void setSourceWell(String sourceWell) {
        sourceWell = WellUtil.Format(sourceWell);
        this.sourceWell.set(sourceWell);
    }

    public void setSourceVolume(double sourceVolume) {
        this.sourceVolume.set(sourceVolume);
    }

    public double getSourceVolume() {
        return sourceVolume.get();
    }

    public StringProperty sourceWellProperty() {
        return sourceWell;
    }

    public DoubleProperty sourceVolumeProperty() {
        return sourceVolume;
    }

    public double getTargetConcentration() {
        return targetConcentration.get();
    }

    public DoubleProperty targetConcentrationProperty() {
        return targetConcentration;
    }

    public void setTargetConcentration(double targetConcentration) {
        this.targetConcentration.set(targetConcentration);
        update();
    }

    public String getSourceBarcode() {
        return sourceBarcode.get();
    }

    public StringProperty sourceBarcodeProperty() {
        return sourceBarcode;
    }

    public void setSourceBarcode(String sourceBarcode) {
        this.sourceBarcode.set(sourceBarcode);
    }

    public double getNewSourceVolume() {
        return newSourceVolume.get();
    }

    public DoubleProperty newSourceVolumeProperty() {
        return newSourceVolume;
    }

    public void setNewSourceVolume(double newSourceVolume) {
        this.newSourceVolume.set(newSourceVolume);
    }

    public double getDna() {
        return dna.get();
    }

    public DoubleProperty dnaProperty() {
        return dna;
    }

    public void setDna(double dna) {
        this.dna.set(dna);
    }

    public double getTe() {
        return te.get();
    }

    public DoubleProperty teProperty() {
        return te;
    }

    public void setTe(double te) {
        this.te.set(te);
    }

    public double getSourceConcentration() {
        return sourceConcentration.get();
    }

    public DoubleProperty sourceConcentrationProperty() {
        return sourceConcentration;
    }

    public void setSourceConcentration(double sourceConcentration) {
        this.sourceConcentration.set(sourceConcentration);
    }

    public String getDestinationWell() {
        return destinationWell.get();
    }

    public StringProperty destinationWellProperty() {
        return destinationWell;
    }

    public void setDestinationWell(String destinationWell) {
        destinationWell = WellUtil.Format(destinationWell);
        this.destinationWell.set(destinationWell);
    }

    public double getDestinationVolume() {
        return destinationVolume.get();
    }

    public DoubleProperty destinationVolumeProperty() {
        return destinationVolume;
    }

    public void setDestinationVolume(double destinationVolume) {
        this.destinationVolume.set(destinationVolume);
    }

    public double getDestinationconcentration() {
        return destinationconcentration.get();
    }

    public DoubleProperty destinationconcentrationProperty() {
        return destinationconcentration;
    }

    public void setDestinationconcentration(double destinationconcentration) {
        this.destinationconcentration.set(destinationconcentration);
    }

    private void update() {
        double dna = 1;
        double te = ((getSourceConcentration() * dna) / getTargetConcentration()) - dna;
        if(getSourceConcentration() < 50) {
            te = 9;
        }
        double v2 = dna + te;
        double c2 = getTargetConcentration();
        if(getSourceConcentration() < 50) {
            c2 = (getSourceConcentration() * dna) / v2;
        }
        double newSourceVolume = getSourceVolume() - dna;
        setDna(dna);
        setTe(te);
        setNewSourceVolume(newSourceVolume);
        setDestinationVolume(v2);
        setDestinationconcentration(c2);
    }

    public String[] toWorklistRow() {
        return new String[]{
                "S1", getSourceWell(), "D1", getDestinationWell(), String.valueOf(getTe()), String.valueOf(getDna())
        };
    }

    @Override
    public String sortWell() {
        return getSourceWell();
    }
}
