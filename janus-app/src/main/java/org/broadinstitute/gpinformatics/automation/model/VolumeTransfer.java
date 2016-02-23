package org.broadinstitute.gpinformatics.automation.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Defines a single volume transfer from source to destination tube
 */
public class VolumeTransfer implements WorklistRow, GenericTransfer {

    private final StringProperty sourceWell = new SimpleStringProperty("");
    private final StringProperty sourceBarcode = new SimpleStringProperty("");
    private final StringProperty destinationWell = new SimpleStringProperty("");
    private final StringProperty destinationBarcode = new SimpleStringProperty("");
    private final DoubleProperty sourceVolume = new SimpleDoubleProperty();
    private final DoubleProperty sourceConcentration = new SimpleDoubleProperty();
    private final DoubleProperty destinationVolume = new SimpleDoubleProperty();
    private final DoubleProperty destinationConcentration = new SimpleDoubleProperty();
    private final DoubleProperty newSourceVolume = new SimpleDoubleProperty();
    private final DoubleProperty dna = new SimpleDoubleProperty();

    public String getSourceWell() {
        return sourceWell.get();
    }

    public StringProperty sourceWellProperty() {
        return sourceWell;
    }

    public void setSourceWell(String sourceWell) {
        this.sourceWell.set(sourceWell);
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

    public String getDestinationWell() {
        return destinationWell.get();
    }

    public StringProperty destinationWellProperty() {
        return destinationWell;
    }

    public void setDestinationWell(String destinationWell) {
        this.destinationWell.set(destinationWell);
    }

    public String getDestinationBarcode() {
        return destinationBarcode.get();
    }

    public StringProperty destinationBarcodeProperty() {
        return destinationBarcode;
    }

    public void setDestinationBarcode(String destinationBarcode) {
        this.destinationBarcode.set(destinationBarcode);
    }

    public double getSourceVolume() {
        return sourceVolume.get();
    }

    public DoubleProperty sourceVolumeProperty() {
        return sourceVolume;
    }

    public void setSourceVolume(double sourceVolume) {
        this.sourceVolume.set(sourceVolume);
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
        update();
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

    public double getDestinationVolume() {
        return destinationVolume.get();
    }

    public DoubleProperty destinationVolumeProperty() {
        return destinationVolume;
    }

    public void setDestinationVolume(double destinationVolume) {
        this.destinationVolume.set(destinationVolume);
    }

    public double getDestinationConcentration() {
        return destinationConcentration.get();
    }

    public DoubleProperty destinationConcentrationProperty() {
        return destinationConcentration;
    }

    public void setDestinationConcentration(double destinationConcentration) {
        this.destinationConcentration.set(destinationConcentration);
    }

    private void update() {
        setNewSourceVolume(getSourceVolume() - getDna());
        setDestinationVolume(getDna());
    }

    @Override
    public String[] toWorklistRow() {
        return new String[]{
                "SRC", getSourceWell(), "DEST", getSourceWell(), String.valueOf(getDna())
        };
    }

    @Override
    public String sortWell() {
        return getSourceWell();
    }
}
