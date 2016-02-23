package org.broadinstitute.gpinformatics.automation.util;

/**
 * Models CSV output of server.exe
 */
public class DatapaqRackScanResult {
    private String rackBarcode;
    private String row;
    private int column;
    private String tubeBarcode;

    public DatapaqRackScanResult() {
    }

    public String getRackBarcode() {
        return rackBarcode;
    }

    public void setRackBarcode(String rackBarcode) {
        this.rackBarcode = rackBarcode;
    }

    public String getRow() {
        return row;
    }

    public void setRow(String row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public String getTubeBarcode() {
        return tubeBarcode;
    }

    public void setTubeBarcode(String tubeBarcode) {
        this.tubeBarcode = tubeBarcode;
    }

    public String getWell() {
        return getRow() + getColumn();
    }

    @Override
    public String toString() {
        return "DatapaqRackScanResult{" +
               "rackBarcode='" + rackBarcode + '\'' +
               ", row='" + row + '\'' +
               ", column=" + column +
               ", tubeBarcode='" + tubeBarcode + '\'' +
               '}';
    }
}