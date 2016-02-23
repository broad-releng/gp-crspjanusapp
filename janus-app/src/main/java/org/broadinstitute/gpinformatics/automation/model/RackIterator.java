package org.broadinstitute.gpinformatics.automation.model;

import java.util.Iterator;

/**
 * Iterate over tubes in rack
 */
public class RackIterator implements Iterator<Tube> {
    private Rack rack;
    private int currentRow;
    private int currentColumn;

    public RackIterator(Rack rack) {
        this.rack = rack;
        this.currentRow = 0;
        this.currentColumn = 0;
    }

    @Override
    public boolean hasNext() {
        return this.currentColumn < rack.getColumns();
    }

    @Override
    public Tube next() {
        String barcode = rack.getTubeAt(currentRow, currentColumn);
        int oldRow = currentRow;
        int oldColumn = currentColumn;
        currentRow++;

        if(currentRow >= rack.getRows()) {
            currentRow = 0;
            currentColumn++;
        }

        return new Tube(oldRow, oldColumn, barcode);
    }

    @Override
    public void remove() {

    }
}
