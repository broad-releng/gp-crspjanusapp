package org.broadinstitute.gpinformatics.automation.model;

/**
 * Models a physical tube
 */
public class Tube {
    int row;
    int column;
    String barcode;

    public Tube(int row, int column, String barcode) {
        this.row = row;
        this.column = column;
        this.barcode = barcode;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getWell() {
        return String.valueOf((char) (getRow() + 65)) + (getColumn() + 1);
    }
}
