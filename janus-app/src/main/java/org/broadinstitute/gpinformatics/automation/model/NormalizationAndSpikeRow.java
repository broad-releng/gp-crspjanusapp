package org.broadinstitute.gpinformatics.automation.model;


import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Defines a janus spike in transfer or eb addition row in a csv or tableView
 */
public class NormalizationAndSpikeRow implements WorklistRow {

    private final ObjectProperty<TransferType> transferType = new SimpleObjectProperty<>();
    private final StringProperty daughterWell = new SimpleStringProperty("");
    private final StringProperty daughterBarcode = new SimpleStringProperty("");
    private final StringProperty parentWell = new SimpleStringProperty("");
    private final StringProperty parentBarcode = new SimpleStringProperty("");
    private final DoubleProperty parentVolume = new SimpleDoubleProperty();
    private final DoubleProperty parentConcentration = new SimpleDoubleProperty();
    private final DoubleProperty daughterVolume = new SimpleDoubleProperty();
    private final DoubleProperty daughterConcentration = new SimpleDoubleProperty();
    private final DoubleProperty newDaughterVolume = new SimpleDoubleProperty();
    private final DoubleProperty newDaughterConcentration = new SimpleDoubleProperty();
    private final DoubleProperty newParentVolume = new SimpleDoubleProperty();
    private final DoubleProperty dna = new SimpleDoubleProperty();
    private final DoubleProperty spikeTargetConcentration = new SimpleDoubleProperty();
    private final DoubleProperty normTargetConcentration = new SimpleDoubleProperty();
    private final DoubleProperty te = new SimpleDoubleProperty();
    private final double spikeMaxConcentration;
    private final double normMinConcentration;

    public NormalizationAndSpikeRow(double spikeMaxConcentration, double normMinConcentration) {
        this.spikeMaxConcentration = spikeMaxConcentration;
        this.normMinConcentration = normMinConcentration;
    }

    public TransferType getTransferType() {
        return transferType.get();
    }

    public ObjectProperty<TransferType> transferTypeProperty() {
        return transferType;
    }

    public void setTransferType(TransferType transferType) {
        this.transferType.set(transferType);
    }

    public String getDaughterWell() {
        return daughterWell.get();
    }

    public StringProperty daughterWellProperty() {
        return daughterWell;
    }

    public void setDaughterWell(String daughterWell) {
        this.daughterWell.set(daughterWell);
    }

    public String getDaughterBarcode() {
        return daughterBarcode.get();
    }

    public StringProperty daughterBarcodeProperty() {
        return daughterBarcode;
    }

    public void setDaughterBarcode(String daughterBarcode) {
        this.daughterBarcode.set(daughterBarcode);
    }

    public String getParentWell() {
        return parentWell.get();
    }

    public StringProperty parentWellProperty() {
        return parentWell;
    }

    public void setParentWell(String parentWell) {
        this.parentWell.set(parentWell);
    }

    public String getParentBarcode() {
        return parentBarcode.get();
    }

    public StringProperty parentBarcodeProperty() {
        return parentBarcode;
    }

    public void setParentBarcode(String parentBarcode) {
        this.parentBarcode.set(parentBarcode);
    }

    public double getDaughterVolume() {
        return daughterVolume.get();
    }

    public DoubleProperty daughterVolumeProperty() {
        return daughterVolume;
    }

    public void setDaughterVolume(double daughterVolume) {
        this.daughterVolume.set(daughterVolume);
    }

    public double getDaughterConcentration() {
        return daughterConcentration.get();
    }

    public DoubleProperty daughterConcentrationProperty() {
        return daughterConcentration;
    }

    public void setDaughterConcentration(double daughterConcentration) {
        this.daughterConcentration.set(daughterConcentration);
    }

    public double getNewParentVolume() {
        return newParentVolume.get();
    }

    public DoubleProperty newParentVolumeProperty() {
        return newParentVolume;
    }

    public void setNewParentVolume(double newParentVolume) {
        this.newParentVolume.set(newParentVolume);
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

    public double getSpikeTargetConcentration() {
        return spikeTargetConcentration.get();
    }

    public DoubleProperty spikeTargetConcentrationProperty() {
        return spikeTargetConcentration;
    }

    public void setSpikeTargetConcentration(double spikeTargetConcentration) {
        this.spikeTargetConcentration.set(spikeTargetConcentration);
        update();
    }

    public double getNormTargetConcentration() {
        return normTargetConcentration.get();
    }

    public DoubleProperty normTargetConcentrationProperty() {
        return normTargetConcentration;
    }

    public void setNormTargetConcentration(double normTargetConcentration) {
        this.normTargetConcentration.set(normTargetConcentration);
        update();
    }

    public double getSpikeMaxConcentration() {
        return spikeMaxConcentration;
    }

    public double getNormMinConcentration() {
        return normMinConcentration;
    }

    public double getParentVolume() {
        return parentVolume.get();
    }

    public DoubleProperty parentVolumeProperty() {
        return parentVolume;
    }

    public void setParentVolume(double parentVolume) {
        this.parentVolume.set(parentVolume);
    }

    public double getParentConcentration() {
        return parentConcentration.get();
    }

    public DoubleProperty parentConcentrationProperty() {
        return parentConcentration;
    }

    public void setParentConcentration(double parentConcentration) {
        this.parentConcentration.set(parentConcentration);
    }

    public double getNewDaughterVolume() {
        return newDaughterVolume.get();
    }

    public DoubleProperty newDaughterVolumeProperty() {
        return newDaughterVolume;
    }

    public void setNewDaughterVolume(double newDaughterVolume) {
        this.newDaughterVolume.set(newDaughterVolume);
    }

    public double getNewDaughterConcentration() {
        return newDaughterConcentration.get();
    }

    public DoubleProperty newDaughterConcentrationProperty() {
        return newDaughterConcentration;
    }

    public void setNewDaughterConcentration(double newDaughterConcentration) {
        this.newDaughterConcentration.set(newDaughterConcentration);
    }

    @Override
    public String[] toWorklistRow() {
        if(getTransferType() == TransferType.SPIKE) {
            return new String[]{
                "P1", getParentWell(), "D1", getDaughterWell(), String.valueOf(getDna())
            };
        } else if(getTransferType() == TransferType.NORMALIZATION) {
            return new String[]{
                "EB", getDaughterWell(), "D1", getDaughterWell(), String.valueOf(getTe())
            };
        }

        return null;
    }

    public String[] toDilutionWorklistRow() {
        if(getTransferType() == TransferType.SPIKE) {
            return new String[]{
                    "P1", getParentWell(), "D1", getDaughterWell(), String.valueOf(getDna()), "0.0"
            };
        } else if(getTransferType() == TransferType.NORMALIZATION) {
            return new String[]{
                    "D1", getDaughterWell(), "D1", getDaughterWell(), "0.0", String.valueOf(getTe())
            };
        }

        return null;
    }

    @Override
    public String sortWell() {
        return getDaughterWell();
    }

    /**
     * samples with Conc above norm min need to have TE added to them
     * samples below spike max need to have DNA from parent added to them
     * samples in between will have no action taken.
     *
     * Spikes will require a parent tube. throws RuntimeException if non-present
     */
    public void update() {
        if(transferType.get() != null) {
            if(transferType.get() == TransferType.SPIKE) {
                handleSpikeUpdate();
            }else if(transferType.get() == TransferType.NORMALIZATION) {
                handleNormUpdate();
            }
        }
    }

    private void handleSpikeUpdate() {
        double dnaToSpike = ((getSpikeTargetConcentration() * getDaughterVolume()) -
                             (getDaughterConcentration() * getDaughterVolume())) /
                            (getParentConcentration() - getSpikeTargetConcentration());
        setDna(dnaToSpike);
        setNewParentVolume(getParentVolume() - dnaToSpike);
        setNewDaughterVolume(getDaughterVolume() + dnaToSpike);
        setNewDaughterConcentration(getSpikeTargetConcentration());
    }

    private void handleNormUpdate() {
        double teToAdd = ((getDaughterConcentration() * getDaughterVolume())
                          / getNormTargetConcentration()) - getDaughterVolume();
        setTe(teToAdd);
        setNewDaughterVolume(getDaughterVolume() + teToAdd);
        setNewDaughterConcentration(getNormTargetConcentration());
    }
}
