package org.broadinstitute.gpinformatics.automation.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Defines a transfer row in a csv or tableView
 */
public class DilutionTransfer implements WorklistRow, GenericTransfer {
    private final StringProperty sourceWell = new SimpleStringProperty("");
    private final StringProperty sourceBarcode = new SimpleStringProperty("");
    private final StringProperty destinationWell = new SimpleStringProperty("");
    private final StringProperty destinationBarcode = new SimpleStringProperty("");
    private final DoubleProperty sourceVolume = new SimpleDoubleProperty();
    private final DoubleProperty targetVolume = new SimpleDoubleProperty();
    private final DoubleProperty sourceConcentration = new SimpleDoubleProperty();
    private final DoubleProperty targetConcentration = new SimpleDoubleProperty();
    private final DoubleProperty newSourceVolume = new SimpleDoubleProperty();
    private final DoubleProperty dna = new SimpleDoubleProperty();
    private final DoubleProperty te = new SimpleDoubleProperty();

    public DilutionTransfer() {
        this("", 0, 0);
    }

    public DilutionTransfer(String well, int sourceVolume, int normVolume) {
        setSourceWell(well);
        setSourceVolume(sourceVolume);
        setNewSourceVolume(normVolume);
    }

    public String getSourceWell() {
        return this.sourceWell.get();
    }

    public void setSourceWell(String sourceWell) {
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

    public double getTargetVolume() {
        return targetVolume.get();
    }

    public DoubleProperty targetVolumeProperty() {
        return targetVolume;
    }

    public void setTargetVolume(double targetVolume) {
        this.targetVolume.set(targetVolume);
        update();
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
        this.destinationWell.set(destinationWell);
    }

    public String getDestinationBarcode() {
        return destinationBarcode.get();
    }

    @Override
    public double getDestinationVolume() {
        return getTargetVolume();
    }

    @Override
    public double getDestinationConcentration() {
        return getTargetConcentration();
    }

    public StringProperty destinationBarcodeProperty() {
        return destinationBarcode;
    }

    public void setDestinationBarcode(String destinationBarcode) {
        this.destinationBarcode.set(destinationBarcode);
    }

    private void update() {
        double dna = getTargetVolume() * getTargetConcentration() / getSourceConcentration();
        double te = getTargetVolume() - dna;
        double newSourceVolume = getSourceVolume() - dna;
        setDna(dna);
        setTe(te);
        setNewSourceVolume(newSourceVolume);
    }

    public String[] toWorklistRow() {
        return new String[]{
                "SRC", getSourceWell(), "DEST", getSourceWell(), String.valueOf(getTe()), String.valueOf(getDna())
        };
    }

    @Override
    public String sortWell() {
        return getSourceWell();
    }
}
