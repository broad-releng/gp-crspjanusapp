package org.broadinstitute.gpinformatics.automation.model;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Models a physical Rack
 */
public class Rack implements Iterable<Tube>{
    private int rows;
    private int columns;
    private StringProperty[][] tubes;
    private StringProperty barcodeProperty;
    private BooleanBinding invalid;

    public Rack() {
        this(8,12);
    }

    public Rack (final int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.tubes = new SimpleStringProperty[this.rows][this.columns];
        this.barcodeProperty = new SimpleStringProperty("");
        for(int row = 0; row < this.rows; row++){
            for(int col = 0; col < this.columns; col++){
                this.tubes[row][col] = new SimpleStringProperty(null);
            }
        }

        invalid = Bindings.createBooleanBinding(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return !barcodeProperty.get().matches("\\d{12}|AB{8}") ||
                        isEmpty();
            }
        }, barcodeProperty);
    }

    public StringProperty getTubePropertyAt(int row, int col) {
        return this.tubes[row][col];
    }

    public String getTubeAt(int row, int col) {
        return getTubePropertyAt(row, col).get();
    }

    public void addTube(Tube tube){
        addTube(tube.getRow(), tube.getColumn(), tube.getBarcode());
    }

    public void addTube(int row, int col, String barcode) {
        if(row > this.rows || col < 0)
            throw new IllegalArgumentException("Row can't exceed value set in rack " + this.rows);
        if(col > this.columns || col < 0)
            throw new IllegalArgumentException("Column can't exceed value set in rack " + this.columns);

        tubes[row][col].set(barcode);
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

    @Override
    public Iterator<Tube> iterator() {
        return new RackIterator(this);
    }

    public boolean isEmpty() {
        for (Tube tube : this) {
            if(tube.getBarcode() != null)
                return false;
        }
        return true;
    }

    public Boolean isInvalid() {
        return invalid.get();
    }

    public BooleanBinding invalidProperty() {
        return invalid;
    }

    public int getTubeCount() {
        int counter = 0;
        for(Tube tube: this){
            if(tube.getBarcode() != null)
                counter++;
        }
        return counter;
    }

    public List<Tube> getTubes() {
        List<Tube> tubes = new ArrayList<>();
        for(Tube tube: this) {
            if(tube.getBarcode() != null)
                tubes.add(tube);
        }
        return tubes;
    }

    public List<String> getTubeBarcodes() {
        List<Tube> tubes = getTubes();
        List<String> barcodes = new ArrayList<>();
        for(Tube tube : tubes) {
            barcodes.add(tube.getBarcode());
        }

        return barcodes;
    }

    public Map<String, Tube> getBarcodeToTube() {
        Map<String, Tube> barcodeToTube = new HashMap<>();
        for(Tube tube: getTubes()) {
            barcodeToTube.put(tube.getBarcode(), tube);
        }
        return barcodeToTube;
    }

    /**
     * Removes the rack barcode and clears the tube array from this rack
     */
    public void reset() {
        this.setBarcode("");
        for(int row = 0; row < this.rows; row++){
            for(int col = 0; col < this.columns; col++){
                this.tubes[row][col].set(null);
            }
        }
    }

    @Override
    public String toString() {
        return "Rack{" +
               "rows=" + rows +
               ", columns=" + columns +
               ", barcodeProperty=" + barcodeProperty +
               ", invalid=" + invalid +
               '}';
    }
}
