package org.broadinstitute.gpinformatics.automation.model;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * Models a Plate
 */
public class Plate {
    private SimpleStringProperty barcodeProperty;
    private int rows;
    private int columns;
    private StringProperty[][] wells;
    private BooleanBinding invalid;

    private String rackName;
    private String physType;

    public Plate() {
        this(16, 24, "Eppendorf384");
    }

    public Plate(int rows, int columns, String physType) {
        this.rows = rows;
        this.columns = columns;
        this.physType = physType;
        this.wells = new SimpleStringProperty[this.rows][this.columns];
        this.barcodeProperty = new SimpleStringProperty("");
        for(int row = 0; row < this.rows; row++){
            for(int col = 0; col < this.columns; col++){
                this.wells[row][col] = new SimpleStringProperty(null);
            }
        }

        invalid = Bindings.createBooleanBinding(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return !barcodeProperty.get().matches("\\d{12}|AB{8}");
            }
        }, barcodeProperty);
    }

    public void setBarcode(String barcode) {
        this.barcodeProperty.setValue(barcode);
    }

    public String getBarcode() {
        return this.barcodeProperty.getValue();
    }

    public StringProperty getBarcodeProperty() {
        return this.barcodeProperty;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public String getRackName() {
        return rackName;
    }

    public void setRackName(String rackName) {
        this.rackName = rackName;
    }

    public String getPhysType() {
        return physType;
    }

    public void setPhysType(String physType) {
        this.physType = physType;
    }

    public Boolean isInvalid() {
        return invalid.get();
    }

    public BooleanBinding invalidProperty() {
        return invalid;
    }

    public void reset() {
        this.setBarcode("");
    }

    public StringProperty getWellPropertyAt(int row, int col) {
        return this.wells[row][col];
    }

    public void setSampleAtWell(String well) {
        int row = well.charAt(0) - 65;
        int col = Integer.parseInt(well.substring(1)) - 1;
        if(row > this.rows || col < 0)
            throw new IllegalArgumentException("Row can't exceed value set in rack " + this.rows);
        if(col > this.columns || col < 0)
            throw new IllegalArgumentException("Column can't exceed value set in rack " + this.columns);

        wells[row][col].set("Sample");
    }

    @Override
    public String toString() {
        return "Plate{" +
               "barcodeProperty=" + barcodeProperty +
               ", rows=" + rows +
               ", columns=" + columns +
               ", wells=" + Arrays.toString(wells) +
               ", invalid=" + invalid +
               '}';
    }
}
