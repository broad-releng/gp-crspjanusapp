package org.broadinstitute.gpinformatics.automation.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Defines a Normalization row in a CSV or tableView
 */
public class Normalization implements WorklistRow {
    private final StringProperty well = new SimpleStringProperty("");
    private final StringProperty barcode = new SimpleStringProperty("");
    private final DoubleProperty target = new SimpleDoubleProperty();
    private final DoubleProperty v1 = new SimpleDoubleProperty();
    private final DoubleProperty c1 = new SimpleDoubleProperty();
    private final DoubleProperty v2 = new SimpleDoubleProperty();
    private final DoubleProperty c2 = new SimpleDoubleProperty();
    private final DoubleProperty te = new SimpleDoubleProperty(0);
    private final boolean isTargetConcentration;
    private boolean inSpecification;

    public Normalization(boolean isTargetConcentration, boolean inSpecification) {
        this.isTargetConcentration = isTargetConcentration;
        this.inSpecification = inSpecification;
    }

    @Override
    public String sortWell() {
        return getWell();
    }

    public String getWell() {
        return well.get();
    }

    public StringProperty wellProperty() {
        return well;
    }

    public void setWell(String well) {
        this.well.set(well);
    }

    public String getBarcode() {
        return barcode.get();
    }

    public StringProperty barcodeProperty() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode.set(barcode);
    }

    public double getTarget() {
        return target.get();
    }

    public DoubleProperty targetProperty() {
        return target;
    }

    /**
     * Targetting either by Volume or Concentration
     * By Volume:
     *  Only makes sense if target volume is greater than current
     * By Concentration:
     *  Only makes sense if target conc is less than current
     *
     * @param target - value to dilute or raise volume to
     */
    public void setTarget(double target) {
        this.target.set(target);

        if(isTargetConcentration)
            updateByConcentration();
        else
            updateByVolume();
    }

    public double getV1() {
        return v1.get();
    }

    public DoubleProperty v1Property() {
        return v1;
    }

    public void setV1(double v1) {
        this.v1.set(v1);
    }

    public double getC1() {
        return c1.get();
    }

    public DoubleProperty c1Property() {
        return c1;
    }

    public void setC1(double c1) {
        this.c1.set(c1);
    }

    public double getV2() {
        return v2.get();
    }

    public DoubleProperty v2Property() {
        return v2;
    }

    public void setV2(double v2) {
        this.v2.set(v2);
    }

    public double getC2() {
        return c2.get();
    }

    public DoubleProperty c2Property() {
        return c2;
    }

    public void setC2(double c2) {
        this.c2.set(c2);
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

    public boolean isInSpecification() {
        return inSpecification;
    }

    /**
     * Update model if targeting by concentration
     */
    private void updateByConcentration() {
        double v1 = getV1();
        double c1 = getC1();
        double c2 = getTarget();
        inSpecification = c1 <= c2;
        if(inSpecification){
            setTe(0);
            setV2(v1); //Not changing volume or conc
            setC2(c1);
            return;
        }
        double te = ((c1 * v1) / c2) - v1;
        double v2 = v1 + te;
        setC2(c2);
        setV2(v2);
        setTe(te);
    }

    /**
     * Update model if targeting by volume
     */
    private void updateByVolume() {
        double v1 = getV1();
        inSpecification = v1 >= getTarget();
        if(inSpecification){
            setTe(0);
            setC2(getC1());
            setV2(v1);
            return;
        }
        double c1 = getC1();
        double te = getTarget() - v1;
        double c2 = (v1 * c1) / getTarget();
        double v2 = v1 + te;
        setC2(c2);
        setV2(v2);
        setTe(te);
    }

    public String[] toWorklistRow() {
        return new String[]{
                "SRC", getWell(), String.valueOf(getTe())
        };
    }

    @Override
    public String toString() {
        return "Normalization{" +
               "well=" + well +
               ", barcode=" + barcode +
               ", target=" + target +
               ", v2=" + v2 +
               ", te=" + te +
               '}';
    }
}
